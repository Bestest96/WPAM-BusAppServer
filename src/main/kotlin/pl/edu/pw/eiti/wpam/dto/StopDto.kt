package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.BusStop

data class StopDto(val fullId: Int,
                   val x: Double,
                   val y: Double,
                   val name: String,
                   val city: String?) {

    constructor(busStop: BusStop): this(busStop.fullId, busStop.x, busStop.y,
            busStop.busStopName.name, busStop.busStopName.city)
}