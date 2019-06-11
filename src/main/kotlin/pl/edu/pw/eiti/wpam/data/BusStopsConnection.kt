package pl.edu.pw.eiti.wpam.data

import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "bus_stops_connections")
data class BusStopsConnection(@EmbeddedId val busStopsConnectionsKey: BusStopsConnectionKey) {
    @Embeddable
    data class BusStopsConnectionKey(val srcBusStopId: Int,
                                     val dstBusStopId: Int): Serializable
}