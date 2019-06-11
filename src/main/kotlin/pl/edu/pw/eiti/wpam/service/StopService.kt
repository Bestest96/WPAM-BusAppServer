package pl.edu.pw.eiti.wpam.service

import pl.edu.pw.eiti.wpam.data.BusStop
import pl.edu.pw.eiti.wpam.data.Schedule
import pl.edu.pw.eiti.wpam.data.ScheduleStop

interface StopService {
    fun getAllStops(): List<BusStop>
    fun getTopDepartures(startStopId: Int, endStopId: Int): List<ScheduleStop>
    fun getSchedulesForDepartures(departures: List<ScheduleStop>): List<Schedule>
    fun getStopsNamesById(stops: List<Int>): Map<Int, String>
}