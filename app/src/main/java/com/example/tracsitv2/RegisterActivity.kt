package com.example.tracsitv2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tracsitv2.data.Event
import com.example.tracsitv2.data.EventViewModel
import com.example.tracsitv2.databinding.ActivityRegisterBinding
import com.example.tracsitv2.mapsApi.RetrofitInstance
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var eventViewModel: EventViewModel

    private val permission: String = Manifest.permission.READ_CALENDAR
    private val requestCode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        LocationProvider.init(this)

        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        binding.btnSyncAgenda.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            } else {
                readCalendarEvents()
                Toast.makeText(this, "Event Database Updated", Toast.LENGTH_SHORT).show()
            }
        }

        val sharedPref = getSharedPreferences("Pref", Context.MODE_PRIVATE)
        val editorPref = sharedPref.edit()

        binding.btnRegisterReg.setOnClickListener {
            val homeAddress = binding.etHomeAddress.text.toString()
            val workAddress = binding.etWorkAddress.text.toString()

            editorPref.apply{
                putString("homeAddress", homeAddress)
                putString("workAddress", workAddress)
                apply()
            }
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnLoginInstead.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnCurrentLocation.setOnClickListener {
            val apiKey = BuildConfig.API_KEY
            if (LocationProvider.hasLocationPermission(this)) {
                Toast.makeText(this, "Location Requested...", Toast.LENGTH_SHORT).show()
                LocationProvider.getUserLocation(this@RegisterActivity, this) { latitude, longitude ->
                    if (latitude != null && longitude != null) {
                        val latlng = "$latitude,$longitude"
                        lifecycleScope.launchWhenCreated {
                            val response = try {
                                RetrofitInstance.api.getCurrentAddress(
                                    key = apiKey,
                                    latlng = latlng
                                )
                            } catch (e: IOException) {
                                Log.e("mapsAPI", "IOException, no internet connection available")
                                return@launchWhenCreated
                            } catch (e: HttpException) {
                                Log.e("mapsAPI", "HttpException, unexpected response")
                                return@launchWhenCreated
                            }
                            if (response.isSuccessful && response.body() != null) {
                                val locationData = response.body()
                                val currentAddress = locationData?.results?.get(0)?.formatted_address
                                binding.etHomeAddress.setText(currentAddress)
                            } else {
                                Log.e("mapsAPI", "Response not successful")
                            }
                        }

                    } else {
                        println("Failed to obtain location.")
                    }
                }
            } else {
                LocationProvider.requestPermissions(this, this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readCalendarEvents()
    {
        val titleCol = CalendarContract.Events.TITLE
        val startDateCol = CalendarContract.Events.DTSTART
        val endDateCol = CalendarContract.Events.DTEND

        val projection = arrayOf(titleCol, startDateCol, endDateCol)
        val selection = CalendarContract.Events.DELETED + " != 1"

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection, selection, null, null
        )

        val locationHome = binding.etHomeAddress.text.toString()
        val locationWork = binding.etWorkAddress.text.toString()

        val titleColIdx = cursor!!.getColumnIndex(titleCol)
        val startDateColIdx = cursor.getColumnIndex(startDateCol)
        val endDateColIdx = cursor.getColumnIndex(endDateCol)

        while (cursor.moveToNext()) {
            val title = cursor.getString(titleColIdx)
            val startDate = Date(cursor.getLong(startDateColIdx)).time
            val endDate = Date(cursor.getLong(endDateColIdx)).time

            val event = Event(
                0,
                title,
                locationHome,
                locationWork,
                startDate,
                endDate,
                0,
                0
            )
            if (startDate >= System.currentTimeMillis()) {
                eventViewModel.upsertEvent(event)
            }
        }
        cursor.close()
    }
}