package com.example.tracsitv2.mapsApi.DistanceMatrixClasses

data class Element(
    val distance: Distance,
    val duration: Duration,
    val status: String
)