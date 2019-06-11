package pl.edu.pw.eiti.wpam.data

import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromDateConverter
import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromTimestampConverter
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "vehicle_positions")
data class VehiclePosition(@Id val id: Int,
                           val brigade: String,
                           val line: String,
                           @Convert(converter = MillisecondFromTimestampConverter::class)
                           val timestamp: Long,
                           val x: Double,
                           val y: Double,
                           val matchedBrigade: String?,
                           val onTrack: Boolean,
                           val destination: String?,
                           val angle: Int?,
                           val matchedScheduleId: String?,
                           @Convert(converter = MillisecondFromDateConverter::class)
                           val matchedScheduleDate: Long?)