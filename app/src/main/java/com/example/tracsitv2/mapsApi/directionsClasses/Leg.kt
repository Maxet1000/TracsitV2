package com.example.tracsitv2.mapsApi.directionsClasses

data class Leg(
    val arrival_time: ArrivalTime,
    val departure_time: DepartureTime,
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: EndLocation,
    val start_address: String,
    val start_location: StartLocation,
    val steps: List<Step>,
    val traffic_speed_entry: List<Any>,
    val via_waypoint: List<Any>
)