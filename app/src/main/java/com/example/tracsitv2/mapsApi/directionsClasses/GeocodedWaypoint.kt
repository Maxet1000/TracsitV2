package com.example.tracsitv2.mapsApi.directionsClasses

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>
)