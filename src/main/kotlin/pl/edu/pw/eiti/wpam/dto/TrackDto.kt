package pl.edu.pw.eiti.wpam.dto

data class TrackDto(var line: String?,
                    var waitTime: Long?,
                    var stops: List<StopTimeDto?>?,
                    var from: Pair<Double, Double>?,
                    var to: Pair<Double, Double>?,
                    var time: Long?)