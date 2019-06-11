package pl.edu.pw.eiti.wpam.data

import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromDateConverter
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "schedules")
data class Schedule(@EmbeddedId val schedulesKey: ScheduleKey,
                    val overnight: String,
                    val brigade: String?,
                    val startStop: Int,
                    val endStop: Int,
                    val tracked: Boolean) {
    @Embeddable
    data class ScheduleKey(val scheduleId: String,
                           val lineNumber: String,
                           @Convert(converter = MillisecondFromDateConverter::class)
                           val validDate: Long): Serializable
}