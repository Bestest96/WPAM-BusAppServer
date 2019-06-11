package pl.edu.pw.eiti.wpam.service

import org.springframework.stereotype.Service
import pl.edu.pw.eiti.wpam.data.BusStop
import pl.edu.pw.eiti.wpam.data.Schedule
import pl.edu.pw.eiti.wpam.data.ScheduleStop
import pl.edu.pw.eiti.wpam.repository.ScheduleStopsRepository
import pl.edu.pw.eiti.wpam.repository.SchedulesRepository
import pl.edu.pw.eiti.wpam.repository.StopsRepository
import java.time.LocalDateTime

@Service
class StopServiceImpl(private val stopsRepository: StopsRepository,
                      private val schedulesRepository: SchedulesRepository,
                      private val scheduleStopsRepository: ScheduleStopsRepository): StopService {

    override fun getAllStops(): List<BusStop> {
        return stopsRepository.findAll()
    }

    override fun getTopDepartures(startStopId: Int, endStopId: Int): List<ScheduleStop> {
        return scheduleStopsRepository.findTop20ByScheduleStopsKeyStopIdBetweenAndPredictedTimeGreaterThanEqualOrderByPredictedTime(
                startStopId, endStopId, System.currentTimeMillis())
    }

    override fun getSchedulesForDepartures(departures: List<ScheduleStop>): List<Schedule> {
        val topScheduleId = departures.map { x -> Schedule.ScheduleKey(x.scheduleStopsKey.scheduleId,
                x.scheduleStopsKey.line,
                x.scheduleStopsKey.scheduleDate) }
        val schedules = schedulesRepository.findBySchedulesKeyIn(topScheduleId)
        return List(departures.size) { i -> schedules[schedules.map { x -> x.schedulesKey }.indexOf(topScheduleId[i])]}
    }

    override fun getStopsNamesById(stops: List<Int>): Map<Int, String> {
        return stopsRepository.findAllByFullIdIn(stops).map { x -> Pair(x.fullId, x.busStopName.name) }.toMap()
    }
}