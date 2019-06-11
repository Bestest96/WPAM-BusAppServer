package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.ScheduleStop

data class StopTimeDto(val id: Int,
                       val name: String,
                       val scheduledTime: Long,
                       val predictedTime: Long) {
    constructor(stop: ScheduleStop, name: String): this(stop.scheduleStopsKey.stopId, name, stop.scheduleStopsKey.stopTime, stop.predictedTime!!)
}