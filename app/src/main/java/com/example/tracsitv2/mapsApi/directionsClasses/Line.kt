package com.example.tracsitv2.mapsApi.directionsClasses

data class Line(
    val agencies: List<Agency>,
    val name: String,
    val short_name: String,
    val vehicle: Vehicle
)