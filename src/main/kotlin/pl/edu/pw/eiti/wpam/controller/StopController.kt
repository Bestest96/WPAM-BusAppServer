package pl.edu.pw.eiti.wpam.controller

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.eiti.wpam.dto.DestinationDto
import pl.edu.pw.eiti.wpam.dto.StopDto
import pl.edu.pw.eiti.wpam.service.StopService

@RestController("/stop")
@Component
class StopController(private val stopService: StopService) {

    @GetMapping("/stop/getStops")
    fun getStops(): List<StopDto> {
        return stopService.getAllStops().map { x -> StopDto(x) }
    }

    @GetMapping("/stop/getDepartures/{stopId}")
    fun getDepartures(@PathVariable stopId: Int): List<DestinationDto> {
        val stopStart = if (stopId / 10000 == 0) stopId * 100 else stopId
        val endStart = if (stopId / 10000 == 0) stopId * 100 + 99 else stopId
        val departures = stopService.getTopDepartures(stopStart, endStart)
        val schedules = stopService.getSchedulesForDepartures(departures)
        val names = stopService.getStopsNamesById(schedules.map{ x -> x.endStop }.distinct())
        return List(departures.size) { i -> DestinationDto(departures[i], schedules[i], names[schedules[i].endStop] ?: "") }
    }
}