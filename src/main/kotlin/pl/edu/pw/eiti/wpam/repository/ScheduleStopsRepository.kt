package pl.edu.pw.eiti.wpam.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import pl.edu.pw.eiti.wpam.data.ScheduleStop

@RepositoryRestResource(exported = false)
interface ScheduleStopsRepository: JpaRepository<ScheduleStop, ScheduleStop.ScheduleStopKey> {
    fun findTop20ByScheduleStopsKeyStopIdBetweenAndPredictedTimeGreaterThanEqualOrderByPredictedTime(stopIdLow: Int, stopIdHigh: Int, currentTime: Long): List<ScheduleStop>
    fun findAllByScheduleStopsKeyScheduleIdAndScheduleStopsKeyScheduleDateAndScheduleStopsKeyLineOrderByScheduleStopsKeyStopTimeAscPredictedTime
            (scheduleId: String, scheduleDate: Long, line: String): List<ScheduleStop>
    fun findTop100ByScheduleStopsKeyStopIdBetweenAndPredictedTimeGreaterThanEqualOrderByPredictedTime(stopIdLow: Int, stopIdHigh: Int, currentTime: Long): List<ScheduleStop>
    fun findTop100ByScheduleStopsKeyStopIdAndPredictedTimeGreaterThanEqualOrderByPredictedTime(stopId: Int, timestamp: Long): List<ScheduleStop>
    fun findAllByPredictedTimeBetweenOrderByPredictedTime(predTimeLow: Long, predTimeHigh: Long): List<ScheduleStop>

}