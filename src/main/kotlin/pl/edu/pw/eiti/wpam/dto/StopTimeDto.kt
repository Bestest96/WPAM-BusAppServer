package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.ScheduleStop

data class StopTimeDto(val id: Int,
                       val x: Double,
                       val y: Double,
                       val name: String,
                       val scheduledTime: Long,
                       val predictedTime: Long) {
    constructor(stop: ScheduleStop, name: String, coords: Pair<Double, Double>): this(stop.scheduleStopsKey.stopId, coords.first, coords.second, name, stop.scheduleStopsKey.stopTime, stop.predictedTime!!)
}