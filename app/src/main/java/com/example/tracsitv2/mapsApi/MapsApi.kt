package com.example.tracsitv2.mapsApi

import com.example.tracsitv2.mapsApi.DistanceMatrixClasses.DistanceMatrix
import com.example.tracsitv2.mapsApi.directionsClasses.Directions
import com.example.tracsitv2.mapsApi.getLocationClasses.GeoCode
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApi {

    @GET("/maps/api/distancematrix/json")
    suspend fun getTravelTime(@Query("key") key: String, @Query("origins") origins : String,
                        @Query("destinations") destinations : String, @Query("mode") mode : String,
                        @Query("units") units : String) : Response<DistanceMatrix>

    @GET("/maps/api/geocode/json")
    suspend fun getCurrentAddress(@Query("latlng") latlng: String, @Query("key") key: String) : Response<GeoCode>

    @GET("/maps/api/directions/json")
    suspend fun getTransitData(@Query("key") key: String, @Query("origin") origins : String,
                               @Query("destination") destinations : String, @Query("mode") mode : String,
                               @Query("units") units : String, @Query("arrival_time") arrival_time : Long) : Response<Directions>
}