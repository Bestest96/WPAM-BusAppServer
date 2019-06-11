package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.ScheduleStop

data class BusStopDto(val name: String,
                      val stopTime: Long,
                      val predictedTime: Long,
                      val status: Int) {
    constructor(stop: ScheduleStop, name: String, idx: Int): this(name, stop.scheduleStopsKey.stopTime, stop.predictedTime!!,
            if (idx == 0) 3 else stop.status)
}