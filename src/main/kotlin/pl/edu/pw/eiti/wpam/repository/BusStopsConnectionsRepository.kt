package pl.edu.pw.eiti.wpam.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import pl.edu.pw.eiti.wpam.data.BusStopsConnection

@RepositoryRestResource(exported = false)
interface BusStopsConnectionsRepository: JpaRepository<BusStopsConnection, BusStopsConnection.BusStopsConnectionKey>