package pl.edu.pw.eiti.wpam.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import pl.edu.pw.eiti.wpam.data.BusStop

@RepositoryRestResource(exported = false)
interface StopsRepository: JpaRepository<BusStop, Int> {
    fun findAllByFullIdIn(fullId: List<Int>): List<BusStop>
}