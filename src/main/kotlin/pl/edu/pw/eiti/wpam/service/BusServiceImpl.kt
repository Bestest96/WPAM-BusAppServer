package pl.edu.pw.eiti.wpam.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import pl.edu.pw.eiti.wpam.data.ScheduleStop
import pl.edu.pw.eiti.wpam.data.VehiclePosition
import pl.edu.pw.eiti.wpam.repository.ScheduleStopsRepository
import pl.edu.pw.eiti.wpam.repository.StopsRepository
import pl.edu.pw.eiti.wpam.repository.VehiclePositionsRepository

@Service
class BusServiceImpl(private val vehiclePositionsRepository: VehiclePositionsRepository,
                     private val scheduleStopsRepository: ScheduleStopsRepository,
                     private val stopsRepository: StopsRepository): BusService {

    override fun getAllPositions(limit: Long): List<VehiclePosition> {
        return vehiclePositionsRepository.findAllByTimestampGreaterThan(System.currentTimeMillis() - limit)
    }

    override fun getPositionById(id: Int): VehiclePosition {
        return vehiclePositionsRepository.findById(id).
                orElse(VehiclePosition(0, "", "", 0, 0.0, 0.0, null, false, null, null, null, null))
    }

    override fun getStopsVisitedToEnd(vehiclePosition: VehiclePosition): List<ScheduleStop> {
        val stops = scheduleStopsRepository.
                findAllByScheduleStopsKeyScheduleIdAndScheduleStopsKeyScheduleDateAndScheduleStopsKeyLineOrderByScheduleStopsKeyStopTimeAscPredictedTime(
                        vehiclePosition.matchedScheduleId!!, vehiclePosition.matchedScheduleDate!!, vehiclePosition.line)
        val idx = stops.indexOfLast { x -> x.status == ScheduleStop.VISITED } - 1
        return stops.subList(if (idx >= 0) idx else 0, stops.size)
    }

    override fun getStopsNamesById(stops: List<Int>): Map<Int, String> {
        return stopsRepository.findAllByFullIdIn(stops).map { x -> Pair(x.fullId, x.busStopName.name) }.toMap()
    }
}