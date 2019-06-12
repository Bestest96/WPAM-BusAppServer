package pl.edu.pw.eiti.wpam.service

import pl.edu.pw.eiti.wpam.data.ScheduleStop

interface TrackService {

    companion object {
        const val METRES_PER_DEGREE = 111139
        const val MILLISECONDS_PER_METER = 200
        const val ON_FOOT = "FOOT"

        const val MAX_GROUPS = 10000
        const val MAX_RADIUS = 1000

        const val MAX_CHANGES = 3

        val penaltyLow = mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, -1 to 0, -2 to 3)
        val penaltyNormal = mapOf(0 to 0, 1 to 3 * 60 * 1000, 2 to 7 * 60 * 1000, 3 to 12 * 60 * 1000, -1 to 7 * 60 * 1000, -2 to 4)
        val penaltyHigh = mapOf(0 to 0, 1 to 10 * 60 * 1000, 2 to 20 * 60 * 1000, 3 to 30 * 60 * 1000, -1 to 15 * 60 * 1000, -2 to 10)
    }

    data class DijkstraResults(val lines: List<String>,
                               val names: List<List<String>?>,
                               val waitTime: List<Long?>,
                               val stops: List<List<ScheduleStop?>?>,
                               val stopsXY: List<List<Pair<Double, Double>>?>,
                               val from: List<Pair<Double, Double>?>,
                               val to: List<Pair<Double, Double>?>,
                               val time: List<Long?>)
    fun updateNeighboursList()
    fun dijkstra(start: Pair<Int, Int>, end: Pair<Int, Int>, timestamp: Long, penaltyType: String):
            DijkstraResults?
    fun getWalkingTime(x1: Double, y1: Double, x2: Double, y2: Double): Double
    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double
}