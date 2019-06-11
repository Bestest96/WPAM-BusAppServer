package pl.edu.pw.eiti.wpam.data

import pl.edu.pw.eiti.wpam.data.converter.MillisecondFromTimestampConverter
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "last_update_timestamp")
data class LastUpdateTimestamp(@Id val tableName: String,
                               @Convert(converter = MillisecondFromTimestampConverter::class)
                               val lastUpdate: Long?)