package com.example.tracsitv2.mapsApi.getLocationClasses

data class GeoCode(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)