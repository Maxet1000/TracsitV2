package com.example.tracsitv2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat



object LocationProvider {
    private lateinit var locationManager: LocationManager

    fun init(context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: AppCompatActivity, context: Context) {
        var permissionsToRequest = mutableListOf<String>()
        if (!hasLocationPermission(context)) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), 0)
        }
    }

    fun getUserLocation(
        activity: AppCompatActivity,
        context: Context,
        callback: (latitude: Double?, longitude: Double?) -> Unit
    ) {
        if (hasLocationPermission(context)) {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    println("Location changed callback called")
                    println("Updated latitude: ${location.latitude}, longitude: ${location.longitude}")
                    locationManager.removeUpdates(this)
                    callback(location.latitude, location.longitude)
                }
            }

            val provider = LocationManager.GPS_PROVIDER

            try {
                locationManager.requestSingleUpdate(provider, locationListener, null)

            } catch (e: SecurityException) {
                Toast.makeText(context, "Couldn't find the location", Toast.LENGTH_LONG).show()
                callback(null, null)
            }
        } else {
            requestPermissions(activity, context)
            callback(null, null)
        }
    }
}
