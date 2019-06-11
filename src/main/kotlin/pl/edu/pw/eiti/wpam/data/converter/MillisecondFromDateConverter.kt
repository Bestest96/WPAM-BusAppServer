package pl.edu.pw.eiti.wpam.data.converter

import java.sql.Date
import javax.persistence.AttributeConverter

class MillisecondFromDateConverter: AttributeConverter<Long, Date> {
    override fun convertToDatabaseColumn(attribute: Long?): Date? {
        return if (attribute != null) Date(attribute) else null
    }

    override fun convertToEntityAttribute(dbData: Date?): Long? {
        return dbData?.time
    }
}