package com.example.tracsitv2.mapsApi.directionsClasses

data class Directions(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)