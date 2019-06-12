package pl.edu.pw.eiti.wpam.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pl.edu.pw.eiti.wpam.data.BusStop
import pl.edu.pw.eiti.wpam.data.ScheduleStop
import pl.edu.pw.eiti.wpam.repository.BusStopsConnectionsRepository
import pl.edu.pw.eiti.wpam.repository.ScheduleStopsRepository
import pl.edu.pw.eiti.wpam.repository.StopsRepository
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.MAX_CHANGES
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.MAX_GROUPS
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.MAX_RADIUS
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.METRES_PER_DEGREE
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.MILLISECONDS_PER_METER
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.ON_FOOT
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.penaltyHigh
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.penaltyLow
import pl.edu.pw.eiti.wpam.service.TrackService.Companion.penaltyNormal
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.*

@Service
class TrackServiceImpl(private val busStopsConnectionsRepository: BusStopsConnectionsRepository,
                       private val scheduleStopsRepository: ScheduleStopsRepository,
                       private val stopsRepository: StopsRepository) : TrackService {

    var neighbours: List<List<Int>> = emptyList()
    var busStops: Map<Int, BusStop> = emptyMap()
    var walkLengths: Map<Int, Map<Int, Double>> = emptyMap()
    private val rwLock: ReadWriteLock = ReentrantReadWriteLock()

    init {
        updateNeighboursList()
    }

    @Scheduled(cron = "0 0 03 * * ?")
    final override fun updateNeighboursList() {
        rwLock.writeLock().lock()
        busStops = stopsRepository.findAll().associateBy({it.fullId}, {it})
        val indices = busStops.keys
        val edges = busStopsConnectionsRepository.findAll()
        neighbours = List(indices.max()!! + 1)
        { i -> edges.asSequence().filter { x -> x.busStopsConnectionsKey.srcBusStopId == i }.map {
            x -> x.busStopsConnectionsKey.dstBusStopId
        }.toList()}
        val avgX = MutableList<Double?>(MAX_GROUPS) {0.0}
        val avgY = MutableList<Double?>(MAX_GROUPS) {0.0}
        val count = MutableList(MAX_GROUPS) {0}
        busStops.forEach { v -> run { avgX[v.key / 100] = avgX[v.key / 100]?.plus(v.value.x)
                                 avgY[v.key / 100] = avgY[v.key / 100]?.plus(v.value.y)
                                 count[v.key / 100] += 1 } }
        avgX.forEachIndexed { i, v -> if (count[i] == 0) avgX[i] = null else avgX[i] = v!! / count[i] }
        avgY.forEachIndexed { i, v -> if (count[i] == 0) avgY[i] = null else avgY[i] = v!! / count[i] }
        val triples = mutableListOf<Triple<Int, Double, Double>>()
        avgX.forEachIndexed { i, v -> if (v != null) triples.add(Triple(i, v, avgY[i]!!)) }
        val closeGroups = triples.associateBy({ it.first }, { triples.filter { u -> getDistance(it.second, it.third, u.second, u.third) <= MAX_RADIUS }.map { u -> u.first } })
        val indicesLengths = indices.associateBy({ it }, { i -> indices.filter { j -> j / 100 in closeGroups[i / 100] ?: error("") }.map { v -> Pair(v, getWalkingTime((busStops[i] ?: error("")).x, (busStops[i] ?: error("")).y, (busStops[v] ?: error("")).x, (busStops[v] ?: error("")).y)) } })
        walkLengths = indices.associateBy({ it }, { (indicesLengths[it] ?: error("")).associateBy({ x -> x.first }, { x -> x.second }) })
        rwLock.writeLock().unlock()
    }

    override fun dijkstra(start: Pair<Int, Int>, end: Pair<Int, Int>, timestamp: Long, penaltyType: String): TrackService.DijkstraResults? {
        rwLock.readLock().lock()
        val startRange = if (start.first == start.second) Pair(start.first / 100 * 100, start.first / 100 * 100 + 99) else start
        val endRange = if (end.first == end.second) Pair(end.first / 100 * 100, end.first / 100 * 100 + 99) else end
        val distance = Array(neighbours.size) {Double.POSITIVE_INFINITY}
        val penalty = when (penaltyType.toLowerCase()) {
            "low" -> penaltyLow
            "normal" -> penaltyNormal
            "high" -> penaltyHigh
            else -> penaltyNormal
        }
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
        val changes = Array(neighbours.size) {0}
        val toLine = Array(neighbours.size) {""}
        val scheduleStopsHist = Array<ScheduleStop?>(neighbours.size) {null}
        val queue = PriorityQueue<Int>(neighbours.size) { x1, x2 -> (distance[x1] + (penalty[min(changes[x1], MAX_CHANGES)] ?: error(""))).compareTo((distance[x2] + (penalty[min(changes[x2], MAX_CHANGES)] ?: error("")))) }
        neighbours.forEachIndexed { idx, _ -> queue.add(idx) }
        val scheduleStops = scheduleStopsRepository.findAllByPredictedTimeBetweenOrderByPredictedTime(timestamp, timestamp + 5 * 60 * 60 * 1000)
        if (scheduleStops.isEmpty()) {
            rwLock.readLock().unlock()
            return null
        }
        val endLines = scheduleStops.asSequence().filter { x -> x.scheduleStopsKey.stopId >= endRange.first && x.scheduleStopsKey.stopId <= endRange.second }.map { x -> x.scheduleStopsKey.line }.distinct().toList()
        while (!queue.isEmpty())  {
            val current = queue.poll()
            if (current !in busStops.keys) {
                rwLock.readLock().unlock()
                return null
            }
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
                if (lines.last() != ON_FOOT)
                    stops.add(scheduleStops.first { x -> x.scheduleStopsKey.scheduleId == stops.last()!!.scheduleStopsKey.scheduleId
                            && x.scheduleStopsKey.scheduleDate == stops.last()!!.scheduleStopsKey.scheduleDate
                            && x.scheduleStopsKey.line == stops.last()!!.scheduleStopsKey.line
                            && x.scheduleStopsKey.stopId == current
                            && x.predictedTime!! >= stops.last()!!.predictedTime!! })
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
                        if (lines[i] == ON_FOOT)
                            stops[i] = scheduleStops.first { x -> x.scheduleStopsKey.scheduleId == stops[i - 1]!!.scheduleStopsKey.scheduleId
                                    && x.scheduleStopsKey.scheduleDate == stops[i - 1]!!.scheduleStopsKey.scheduleDate
                                    && x.scheduleStopsKey.line == lines[i - 1]
                                    && x.scheduleStopsKey.stopId == orders[i - 1]
                                    && x.predictedTime!! >= stops[i - 1]!!.predictedTime!!}
                    }
                }
                startSequence.add(lines.size - (count + 1))
                endSequence.add(lines.size - 1)
                rwLock.readLock().unlock()
                return TrackService.DijkstraResults(
                        sequenceOfLines,
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else names.subList(startSequence[i], endSequence[i] + 1).toList() }.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else stops[startSequence[i] + 1]!!.predictedTime!! - (timestamp + distances[startSequence[i]])}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else stops.subList(startSequence[i] + 1, endSequence[i] + 2).toList()}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) null else stops.subList(startSequence[i] + 1, endSequence[i] + 2). map { j -> Pair((busStops[j!!.scheduleStopsKey.stopId] ?: error("")).x, (busStops[j.scheduleStopsKey.stopId] ?: error("")).y) } }.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) Pair((busStops[orders[startSequence[i]]] ?: error("")).x, (busStops[orders[startSequence[i]]] ?: error("")).y) else null}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) Pair((busStops[orders[endSequence[i]]] ?: error("")).x, (busStops[orders[endSequence[i]]] ?: error("")).y) else null}.toList(),
                        sequenceOfLines.mapIndexed { i, l -> if (l == ON_FOOT) distances[endSequence[i]] - distances[startSequence[i]] else null}.toList()
                )
            }
            for (walk in walkLengths[current] ?: error("")) {
                val diff = walk.value * (penalty[-2] ?: error(""))
                if (distance[current] + diff < distance[walk.key]) {
                    queue.remove(walk.key)
                    distance[walk.key] = distance[current] + diff
                    previous[walk.key] = current
                    toLine[walk.key] = ON_FOOT
                    queue.add(walk.key)
                }
            }
            val currentStops = scheduleStops.asSequence().filter { x -> x.scheduleStopsKey.stopId == current && (x.predictedTime!! >= timestamp + distance[current]) }.take(100).toList()
            if (currentStops.isEmpty())
                continue
            val uniqueLines = currentStops.map { x -> x.scheduleStopsKey.line }.distinct()
            val prevChanges = changes[current]
            for (neigh in neighbours[current]) {
                val neighStops = scheduleStops.asSequence().filter { x -> (x.scheduleStopsKey.stopId >= neigh / 100 * 100 && x.scheduleStopsKey.stopId <= neigh / 100 * 100 + 99) &&
                        x.predictedTime!! >= (timestamp + distance[current]) && x.scheduleStopsKey.line in uniqueLines }.take(100).toList()
                if (neighStops.isEmpty())
                    continue
                var minDiff = Double.POSITIVE_INFINITY
                var minPenaltyDiff = Double.POSITIVE_INFINITY
                var minLine = ""
                var minScheduleStop: ScheduleStop? = null
                var minChanges = -1
                for (line in uniqueLines) {
                    val firstCurr = currentStops.first { x -> x.scheduleStopsKey.line == line }
                    val neighScheduleIdx = neighStops.indexOfFirst { x -> x.scheduleStopsKey.scheduleId == firstCurr.scheduleStopsKey.scheduleId
                            && x.scheduleStopsKey.scheduleDate == firstCurr.scheduleStopsKey.scheduleDate && x.scheduleStopsKey.line == firstCurr.scheduleStopsKey.line
                            && x.scheduleStopsKey.stopId == neigh}
                    if (neighScheduleIdx == -1)
                        continue
                    val neighScheduleMatch = neighStops[neighScheduleIdx]
                    val newChanges = if ((current >= startRange.first && current <= startRange.second) || line == toLine[current]) prevChanges else prevChanges + 1
                    val diff = neighScheduleMatch.predictedTime!! - (timestamp + distance[current])
                    val endPenalty = if (line in endLines) 0 else 1
                    val penaltyDiff = diff + (penalty[min(newChanges, MAX_CHANGES)] ?: error("")) + endPenalty * (penalty[-1] ?: error(""))
                    if (penaltyDiff < minPenaltyDiff) {
                        minPenaltyDiff = penaltyDiff
                        minDiff = diff
                        minLine = line
                        minScheduleStop = firstCurr
                        minChanges = newChanges
                    }
                }
                if (distance[current] + minDiff < distance[neigh]) {
                    queue.remove(neigh)
                    distance[neigh] = distance[current] + minDiff
                    previous[neigh] = current
                    toLine[neigh] = minLine
                    scheduleStopsHist[neigh] = minScheduleStop
                    changes[neigh] = minChanges
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

    override fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt(((x1 - x2) * METRES_PER_DEGREE).pow(2) + ((y1 - y2) * METRES_PER_DEGREE).pow(2))
    }
}