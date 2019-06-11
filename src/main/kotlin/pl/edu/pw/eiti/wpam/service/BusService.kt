package pl.edu.pw.eiti.wpam.service

import pl.edu.pw.eiti.wpam.data.ScheduleStop
import pl.edu.pw.eiti.wpam.data.VehiclePosition

interface BusService {
    fun getAllPositions(limit: Long): List<VehiclePosition>
    fun getPositionById(id: Int): VehiclePosition
    fun getStopsVisitedToEnd(vehiclePosition: VehiclePosition): List<ScheduleStop>
    fun getStopsNamesById(stops: List<Int>): Map<Int, String>
}