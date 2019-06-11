package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.Schedule
import pl.edu.pw.eiti.wpam.data.ScheduleStop

data class DestinationDto(val lineNumber: String,
                          val brigade: String?,
                          val scheduledTime: Long,
                          val predictedTime: Long?,
                          val destination: String,
                          val isTracked: Boolean) {
    constructor(departure: ScheduleStop, schedule: Schedule, name: String): this(departure.scheduleStopsKey.line,
            schedule.brigade, departure.scheduleStopsKey.stopTime, departure.predictedTime,
            name, schedule.tracked)
}