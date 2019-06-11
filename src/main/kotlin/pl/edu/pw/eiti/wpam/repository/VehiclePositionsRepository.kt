package pl.edu.pw.eiti.wpam.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import pl.edu.pw.eiti.wpam.data.VehiclePosition

@RepositoryRestResource(exported = false)
interface VehiclePositionsRepository: JpaRepository<VehiclePosition, Int> {
    fun findAllByTimestampGreaterThan(time: Long): List<VehiclePosition>
}