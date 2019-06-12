package pl.edu.pw.eiti.wpam.controller

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.eiti.wpam.dto.DestinationDto
import pl.edu.pw.eiti.wpam.dto.GroupStopDto
import pl.edu.pw.eiti.wpam.dto.StopDto
import pl.edu.pw.eiti.wpam.service.StopService

@RestController("/stop")
@Component
class StopController(private val stopService: StopService) {

    @GetMapping("/stop/getStops")
    fun getStops(): List<List<Any>> {
        val stops = stopService.getAllStops()
        val uniqueStops = stops.map { x -> x.busStopName }.distinct().sortedWith(compareBy({ it.name }, { it.city }))
        val avgX = MutableList(uniqueStops.size) {0.0}
        val avgY = MutableList(uniqueStops.size) {0.0}
        stops.forEach { v ->
            avgX[uniqueStops.indexOf(v.busStopName)] += v.x
            avgY[uniqueStops.indexOf(v.busStopName)] += v.y
        }
        avgX.forEachIndexed { i, _ -> avgX[i] = avgX[i] / stops.filter { x -> x.busStopName == uniqueStops[i] }.size}
        avgY.forEachIndexed { i, _ -> avgY[i] = avgY[i] / stops.filter { x -> x.busStopName == uniqueStops[i] }.size}
        return listOf(stops.map { x -> StopDto(x) }, uniqueStops.mapIndexed { i, x -> GroupStopDto(x.id, avgX[i], avgY[i], x.name, x.city!!) })
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