package pl.edu.pw.eiti.wpam.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import pl.edu.pw.eiti.wpam.dto.StopTimeDto
import pl.edu.pw.eiti.wpam.dto.TrackDto
import pl.edu.pw.eiti.wpam.service.TrackService

@RestController("/track")
class TrackController(private val trackService: TrackService) {

    @GetMapping("/track/getRoute/{startStopId}/{endStopId}/{timestamp}")
    fun getRoute(@PathVariable startStopId: Int,
                 @PathVariable endStopId: Int,
                 @PathVariable timestamp: Long,
                 @RequestParam(required = false) penalty: String?): List<TrackDto> {
        val penaltyType = penalty ?: "normal"
        val startIdLow = if (startStopId / 10000 == 0) startStopId * 100 else startStopId
        val endIdLow = if (endStopId / 10000 == 0) endStopId * 100 else endStopId
        val startIdHigh = if (startStopId / 10000 == 0) startStopId * 100 + 99 else startStopId
        val endIdHigh = if (endStopId / 10000 == 0) endStopId * 100 + 99 else endStopId
        val startId = Pair(startIdLow, startIdHigh)
        val endId = Pair(endIdLow, endIdHigh)
        val result = trackService.aStar(startId, endId, timestamp, penaltyType)
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Dijkstra failed to find a solution!")
        return List(result.lines.size) { i -> TrackDto(result.lines[i], result.waitTime[i], if (result.stops[i] == null) null else result.stops[i]!!.mapIndexed { j, x -> StopTimeDto(x!!, result.names[i]!![j], result.stopsXY[i]!![j])}, result.from[i], result.to[i], result.time[i])}
    }
}