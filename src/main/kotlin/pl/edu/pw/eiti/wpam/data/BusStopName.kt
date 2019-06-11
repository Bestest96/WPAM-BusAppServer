package pl.edu.pw.eiti.wpam.data

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "bus_stops_names")
data class BusStopName(@Id val id: Int,
                       val name: String,
                       val city: String?)