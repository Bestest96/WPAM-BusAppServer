package pl.edu.pw.eiti.wpam.dto

data class TrackDto(var line: String?,
                    var waitTime: Long?,
                    var stops: List<StopTimeDto?>?,
                    var fromId: Int?,
                    var toId: Int?,
                    var time: Long?)