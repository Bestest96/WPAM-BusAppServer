package pl.edu.pw.eiti.wpam.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import pl.edu.pw.eiti.wpam.data.Schedule

@RepositoryRestResource(exported = false)
interface SchedulesRepository: JpaRepository<Schedule, Schedule.ScheduleKey> {
    fun findBySchedulesKeyIn(keys: List<Schedule.ScheduleKey>): List<Schedule>
}