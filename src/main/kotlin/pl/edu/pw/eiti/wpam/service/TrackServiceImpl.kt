package pl.edu.pw.eiti.wpam.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pl.edu.pw.eiti.wpam.data.BusStop
import pl.edu.pw.eiti.wpam.data.ScheduleStop
import pl.edu.pw.eiti.wpam.repository.BusStopsConnectionsRepository
import pl.edu.pw.eiti.wpam.repository.ScheduleStopsRepository
import pl.edu.pw.eiti.wpam.repository.StopsRepository
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.METRES_PER_DEGREE
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.MILLISECONDS_PER_METER
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.ON_FOOT
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

@Service
class TrackServiceImpl(private val busStopsConnectionsRepository: BusStopsConnectionsRepository,
                       private val scheduleStopsRepository: ScheduleStopsRepository,
                       private val stopsRepository: StopsRepository) : TrackService {

    var neighbours: Array<Array<Int>> = emptyArray()
    var busStops: Map<Int, BusStop> = emptyMap()
    private val rwLock: ReadWriteLock = ReentrantReadWriteLock()

    init {
        updateNeighboursList()
    }

    @Scheduled(cron = "0 0 03 * * ?")
    final override fun updateNeighboursList() {
        rwLock.writeLock().lock()
        val edges = busStopsConnectionsRepository.findAll()
        neighbours = Array(edges.map { x -> max(x.busStopsConnectionsKey.srcBusStopId,
                x.busStopsConnectionsKey.dstBusStopId) }.max()!!)
        { i -> edges.filter { x -> x.busStopsConnectionsKey.srcBusStopId == i }.map {
            x -> x.busStopsConnectionsKey.dstBusStopId
        }.toTypedArray()}
        busStops = stopsRepository.findAll().associateBy({it.fullId}, {it})
        rwLock.writeLock().unlock()
    }

    override fun dijkstra(startId: Pair<Int, Int>, endId: Pair<Int, Int>, timestamp: Long, reversed: Boolean): TrackService.DijkstraResults? {
        rwLock.readLock().lock()
        val start = if (reversed) endId else startId
        val end = if(reversed) startId else endId
        val distance = Array(neighbours.size) {Double.POSITIVE_INFINITY}
        var avgX = 0.0
        var avgY = 0.0
        val beginStops = busStops.filter { v -> v.key in start.first..start.second }
        beginStops.forEach { v ->
            avgX += v.value.x
            avgY += v.value.y
        }
        avgX /= beginStops.size
        avgY /= beginStops.size
        beginStops.forEach { v -> distance[v.key] = getWalkingTime(v.value.x, v.value.y, avgX, avgY)}
        val previous = Array(neighbours.size) {-1}
        val toLine = Array(neighbours.size) {""}
        val scheduleStopsHist = Array<ScheduleStop?>(neighbours.size) {null}
        val queue = PriorityQueue<Int>(neighbours.size, kotlin.Comparator { x1, x2 -> distance[x1].compareTo(distance[x2]) })
        neighbours.forEachIndexed { idx, _ -> queue.add(idx) }
        while (!queue.isEmpty())  {
            val current = queue.poll()
            if (current in end.first..end.second) {
                val distances = mutableListOf<Long>()
                val orders = mutableListOf<Int>()
                val lines = mutableListOf<String>()
                val stops = mutableListOf<ScheduleStop?>()
                val names = mutableListOf<String>()
                var toCheck = current
                while (toCheck != -1) {
                    distances.add(distance[toCheck].roundToLong())
                    orders.add(toCheck)
                    lines.add(toLine[toCheck])
                    stops.add(scheduleStopsHist[toCheck])
                    names.add((busStops[toCheck] ?: error("")).busStopName.name)
                    toCheck = previous[toCheck]
                }
                distances.reverse()
                orders.reverse()
                lines.reverse()
                stops.reverse()
                names.reverse()
                val sequenceOfLines = mutableListOf(lines[1])
                val startSequence = mutableListOf<Int>()
                val endSequence = mutableListOf<Int>()
                var count = 1
                for (i in 2 until lines.size) {
                    if (lines[i] == sequenceOfLines.last())
                        ++count
                    else {
                        startSequence.add(i - (count + 1))
                        endSequence.add(i - 1)
                        sequenceOfLines.add(lines[i])
                        count = 1
                    }
                }
                startSequence.add(lines.size - (count + 1))
                endSequence.add(lines.size - 1)
                rwLock.readLock().unlock()
                return TrackService.DijkstraResults(
                        sequenceOfLines,
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else names.subList(startSequence[i], endSequence[i] + 1).toList() }.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else stops[startSequence[i] + 1]!!.predictedTime!! - (timestamp + distances[startSequence[i]])}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else stops.subList(startSequence[i] + 1, endSequence[i] + 1).toList()}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) orders[startSequence[i]] else null}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) orders[endSequence[i]] else null}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) distances[endSequence[i]] - distances[startSequence[i]] else null}.toList()
                )
            }
            val curMain = current / 100
            for (other in busStops.keys.filter { x -> x / 100 == curMain }) {
                val walkingTime = getWalkingTime((busStops[current] ?: error("")).x, (busStops[current] ?: error("")).y,
                        (busStops[other] ?: error("")).x, (busStops[other] ?: error("")).y)
                if (distance[current] + walkingTime < distance[other]) {
                    queue.remove(other)
                    distance[other] = distance[current] + walkingTime
                    previous[other] = current
                    toLine[other] = ON_FOOT
                    queue.add(other)
                }
            }
            val currentStops = scheduleStopsRepository.findTop100ByScheduleStopsKeyStopIdAndPredictedTimeGreaterThanEqualOrderByPredictedTime(
                    current, (timestamp + distance[current]).roundToLong()
            )
            val uniqueLines = currentStops.map { x -> x.scheduleStopsKey.line }.distinct()
            for (neigh in neighbours[current]) {
                val neighStops = scheduleStopsRepository.findTop100ByScheduleStopsKeyStopIdBetweenAndPredictedTimeGreaterThanEqualOrderByPredictedTime(
                        (neigh / 100) * 100, (neigh / 100) * 100 + 99, (timestamp + distance[current]).roundToLong()
                )
                var minDiff = Double.POSITIVE_INFINITY
                var minLine = ""
                var minScheduleStop: ScheduleStop? = null
                for (line in uniqueLines) {
                    val firstCurr = currentStops.first { x -> x.scheduleStopsKey.line == line }
                    val neighScheduleIdx = neighStops.indexOfFirst { x -> x.scheduleStopsKey.scheduleId == firstCurr.scheduleStopsKey.scheduleId
                            && x.scheduleStopsKey.scheduleDate == firstCurr.scheduleStopsKey.scheduleDate && x.scheduleStopsKey.line == firstCurr.scheduleStopsKey.line}
                    if (neighScheduleIdx == -1)
                        continue
                    val neighScheduleMatch = neighStops[neighScheduleIdx]
                    val diff = neighScheduleMatch.predictedTime!! - (timestamp + distance[current])
                    if (diff < minDiff) {
                        minDiff = diff
                        minLine = line
                        minScheduleStop = firstCurr
                    }
                }
                if (distance[current] + minDiff < distance[neigh]) {
                    queue.remove(neigh)
                    distance[neigh] = distance[current] + minDiff
                    previous[neigh] = current
                    toLine[neigh] = minLine
                    scheduleStopsHist[neigh] = minScheduleStop
                    queue.add(neigh)
                }
            }
        }
        rwLock.readLock().unlock()
        return null
    }

    override fun getWalkingTime(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val metres = (abs(x1 - x2) + abs(y1 - y2)) * METRES_PER_DEGREE
        return metres * MILLISECONDS_PER_METER
    }
}