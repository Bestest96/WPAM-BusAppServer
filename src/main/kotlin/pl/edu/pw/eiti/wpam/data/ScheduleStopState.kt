package pl.edu.pw.eiti.wpam.data

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "schedule_stop_states")
data class ScheduleStopState(@Id val id: Int,
                             val state: String)