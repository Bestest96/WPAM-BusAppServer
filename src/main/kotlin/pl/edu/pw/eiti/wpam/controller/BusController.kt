package pl.edu.pw.eiti.wpam.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import pl.edu.pw.eiti.wpam.dto.BusStopDto
import pl.edu.pw.eiti.wpam.dto.VehiclePositionDto
import pl.edu.pw.eiti.wpam.service.BusService

@RestController("/bus")
class BusController(private val busService: BusService) {
    @GetMapping("/bus/getPositions")
    fun getPositions(@RequestParam(required = false) limit: Long?): List<VehiclePositionDto> {
        val lim = limit ?: 5 * 60 * 1000
        return busService.getAllPositions(lim).map { x -> VehiclePositionDto(x) }
    }

    @GetMapping("/bus/getBusData/{busId}")
    fun getBusData(@PathVariable busId: Int): List<BusStopDto> {
        val vehiclePos = busService.getPositionById(busId)
        if (vehiclePos.matchedBrigade == null)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Brigade not matched!")
        else if (!vehiclePos.onTrack)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Not tracked!")
        else if (vehiclePos.matchedScheduleId == null || vehiclePos.matchedScheduleDate == null)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No matched schedule id or date!")
        val stops = busService.getStopsVisitedToEnd(vehiclePos)
        val names = busService.getStopsNamesById(stops.map { x -> x.scheduleStopsKey.stopId })
        return List(stops.size) { i -> BusStopDto(stops[i], names[stops[i].scheduleStopsKey.stopId] ?: "", i) }
    }

    @GetMapping("bus/getLinesFromPos")
    fun getLinesFromPositions(): List<String> {
        return busService.getAllPositions(System.currentTimeMillis()).map { x -> x.line }.distinct()
    }
}