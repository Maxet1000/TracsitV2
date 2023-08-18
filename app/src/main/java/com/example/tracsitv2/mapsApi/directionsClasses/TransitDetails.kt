package com.example.tracsitv2.mapsApi.directionsClasses

data class TransitDetails(
    val arrival_stop: ArrivalStop,
    val arrival_time: ArrivalTime,
    val departure_stop: DepartureStop,
    val departure_time: DepartureTime,
    val headsign: String,
    val line: Line,
    val num_stops: Int,
    val trip_short_name: String
)