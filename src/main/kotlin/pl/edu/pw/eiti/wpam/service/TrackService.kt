package pl.edu.pw.eiti.wpam.service

import pl.edu.pw.eiti.wpam.data.ScheduleStop

interface TrackService {

    companion object {
        const val METRES_PER_DEGREE = 111139
        const val MILLISECONDS_PER_METER = 600
        const val ON_FOOT = "FOOT"
    }

    data class DijkstraResults(val lines: List<String>,
                               val names: List<List<String>?>,
                               val waitTime: List<Long?>,
                               val stops: List<List<ScheduleStop?>?>,
                               val from: List<Int?>,
                               val to: List<Int?>,
                               val time: List<Long?>)
    fun updateNeighboursList()
    fun dijkstra(startId: Pair<Int, Int>, endId: Pair<Int, Int>, timestamp: Long, reversed: Boolean):
            DijkstraResults?
    fun getWalkingTime(x1: Double, y1: Double, x2: Double, y2: Double): Double
}