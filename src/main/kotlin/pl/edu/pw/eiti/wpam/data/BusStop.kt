package pl.edu.pw.eiti.wpam.data

import javax.persistence.*

@Entity
@Table(name = "bus_stops")
data class BusStop(@Id val fullId: Int,
                   val id: Int,
                   val directionId: Int,
                   val x: Double,
                   val y: Double,
                   @ManyToOne(fetch = FetchType.LAZY)
                   @JoinColumn(name = "id", insertable = false, updatable = false)
                   val busStopName: BusStopName)