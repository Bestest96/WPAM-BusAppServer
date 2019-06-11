package pl.edu.pw.eiti.wpam.data

import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromDateConverter
import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromTimestampConverter
import java.io.Serializable
import javax.persistence.*


@Entity
@Table(name = "schedule_stops")
data class ScheduleStop(@EmbeddedId val scheduleStopsKey: ScheduleStopKey,
                        val nextDay: Boolean,
                        @Convert(converter = MillisecondFromTimestampConverter::class)
                        val predictedTime: Long?,
                        val status: Int) {
    @Embeddable
    data class ScheduleStopKey(val scheduleId: String,
                               @Convert(converter = MillisecondFromDateConverter::class)
                               val scheduleDate: Long,
                               val stopId: Int,
                               @Convert(converter = MillisecondFromTimestampConverter::class)
                               val stopTime: Long,
                               val line: String): Serializable
    companion object {
        const val NOT_VISITED = 1
        const val MAYBE_VISITED = 2
        const val VISITED = 3
    }


}