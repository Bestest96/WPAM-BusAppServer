package pl.edu.pw.eiti.wpam.dto

import pl.edu.pw.eiti.wpam.data.VehiclePosition

data class VehiclePositionDto(val id: Int,
                              val brigade: String,
                              val line: String,
                              val timestamp: Long,
                              val x: Double,
                              val y: Double,
                              val matchedBrigade: String?,
                              val onTrack: Boolean,
                              val destination: String?,
                              val angle: Int?) {
    constructor(vehiclePosition: VehiclePosition): this(vehiclePosition.id,
            vehiclePosition.brigade, vehiclePosition.line, vehiclePosition.timestamp,
            vehiclePosition.x, vehiclePosition.y, vehiclePosition.matchedBrigade,
            vehiclePosition.onTrack, vehiclePosition.destination, vehiclePosition.angle)
}