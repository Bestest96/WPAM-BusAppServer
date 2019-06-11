package pl.edu.pw.eiti.wpam.data.converter

import java.sql.Timestamp
import javax.persistence.AttributeConverter

class MillisecondFromTimestampConverter: AttributeConverter<Long, Timestamp> {
    override fun convertToDatabaseColumn(attribute: Long?): Timestamp? {
        return if (attribute != null) Timestamp(attribute) else null
    }

    override fun convertToEntityAttribute(dbData: Timestamp?): Long? {
        return dbData?.time
    }
}