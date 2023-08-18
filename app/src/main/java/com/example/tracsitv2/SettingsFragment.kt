package com.example.tracsitv2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tracsitv2.data.EventViewModel
import com.example.tracsitv2.databinding.FragmentSettingsBinding
import com.example.tracsitv2.mapsApi.RetrofitInstance
import com.example.tracsitv2.notifications.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var eventViewModel: EventViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        val sharedPreferences = activity?.getSharedPreferences("Pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        var homeAddress = sharedPreferences?.getString("homeAddress", "")
        var workAddress = sharedPreferences?.getString("workAddress", "")

        binding.etHomeAddressSet.setText(homeAddress)
        binding.etWorkAddressSet.setText(workAddress)

        binding.btnUpdateSet.setOnClickListener {
            homeAddress = binding.etHomeAddressSet.text.toString()
            workAddress = binding.etWorkAddressSet.text.toString()

            editor?.apply {
                putString("homeAddress", homeAddress)
                putString("workAddress", workAddress)
                apply()
            }
            Toast.makeText(context, "Settings Updated", Toast.LENGTH_SHORT).show()
        }

        binding.btnAutoSet.setOnClickListener {
            if (homeAddress != null && workAddress != null) {
                viewLifecycleOwner.lifecycleScope.launch { setAllCommutes(homeAddress!!, workAddress!!) }
            } else Toast.makeText(context, "set the home and work addresses", Toast.LENGTH_SHORT).show()

        }
    }

    fun setAllCommutes(homeAddress: String, workAddress: String) {
        val allEventsLiveData = eventViewModel.getAllEventsOrderedByEventStartTime
        createNotificationChannel()
        val checkedRadioButtonId = binding.rgSettings.checkedRadioButtonId
        val mode = binding.rgSettings.findViewById<RadioButton>(checkedRadioButtonId).text.toString()

        allEventsLiveData.observe(viewLifecycleOwner, Observer { allEvents ->
            var lastEventEndTime : Long = 0
            for (event in allEvents) {
                if (event.commuteEndTime == 0L  && !onSameDay(event.eventStartTime, lastEventEndTime)) {
                    event.startLocation = homeAddress
                    event.endLocation = workAddress
                    if (mode == "transit"){
                        viewLifecycleOwner.lifecycleScope.launch {
                            val depAndArr = getTransitDepAndArr(event.startLocation, event.endLocation, event.eventStartTime/1000)
                            event.commuteStartTime = depAndArr[0]*1000
                            event.commuteEndTime = depAndArr[1]*1000
                            eventViewModel.upsertEvent(event)
                            scheduleNotification(
                                event.commuteStartTime - 60 * 60 * 1000,
                                event.name
                            )
                        }
                    } else {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val travelTime = getTravelTime(event.startLocation, event.endLocation, mode)
                            event.commuteStartTime = event.eventStartTime - travelTime
                            event.commuteEndTime = event.eventStartTime
                            eventViewModel.upsertEvent(event)
                            scheduleNotification(
                                event.commuteStartTime - 60 * 60 * 1000,
                                event.name
                            )
                        }
                    }
                }
                lastEventEndTime = event.eventEndTime
            }

        })

        Toast.makeText(context, "Commutes and notifications set!", Toast.LENGTH_SHORT).show()
    }

    suspend fun getTravelTime(origin: String, destination: String, mode: String): Long {

            var travelTimeInMilliseconds = 0L
            val apiKey = BuildConfig.API_KEY
            try {
                val response = RetrofitInstance.api.getTravelTime(
                    key = apiKey,
                    destinations = destination,
                    origins = origin,
                    units = "metric",
                    mode = mode
                )

                if (response.isSuccessful && response.body() != null) {
                    val travelData = response.body()
                    if (travelData?.status == "OK") {
                        if (travelData.rows[0].elements[0].status == "OK") {
                            val travelDurationSeconds =
                                travelData.rows[0].elements[0].duration.value
                            travelTimeInMilliseconds = travelDurationSeconds * 1000L
                        }
                    }
                } else {
                    Log.e("mapsAPI", "Response not successful")
                }
            } catch (e: IOException) {
                Log.e("mapsAPI", "IOException, no internet connection available")
            } catch (e: HttpException) {
                Log.e("mapsAPI", "HttpException, unexpected response")
            }

            return travelTimeInMilliseconds

    }

    suspend fun getTransitDepAndArr(origin: String, destination: String, arrival_time: Long): List<Long> {
        var depAndArrTime : List<Long>? = null
        try {
            val apiKey = BuildConfig.API_KEY
            val response = RetrofitInstance.api.getTransitData(
                key = apiKey,
                destinations = destination,
                origins = origin,
                units = "metric",
                mode = "transit",
                arrival_time = arrival_time
            )

            if (response.isSuccessful && response.body() != null) {
                val travelData = response.body()
                if (travelData?.status == "OK") {
                    if (travelData.geocoded_waypoints[0].geocoder_status == "OK") {
                        val departureTimeSeconds = travelData.routes[0].legs[0].departure_time.value.toLong()
                        val arrivalTimeSeconds = travelData.routes[0].legs[0].arrival_time.value.toLong()
                        depAndArrTime = listOf(departureTimeSeconds, arrivalTimeSeconds)
                    }
                }
            } else {
                Log.e("mapsAPI", "Response not successful")
            }
        } catch (e: IOException) {
            Log.e("mapsAPI", "IOException, no internet connection available")
        } catch (e: HttpException) {
            Log.e("mapsAPI", "HttpException, unexpected response")
        }

        return depAndArrTime ?: emptyList()

    }


    fun onSameDay(time1: Long, time2: Long) : Boolean{
        val day1 = time1 /(1000*60*60*24)
        val day2 = time2 /(1000*60*60*24)
        return day1 == day2
    }

    private fun scheduleNotification(time: Long, name: String) {
        val intent = Intent((activity as PrimaryActivity).applicationContext, Notification::class.java)
        intent.putExtra(titleExtra, name)

        val pendingIntent = PendingIntent.getBroadcast(
            (activity as PrimaryActivity).applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = (activity as PrimaryActivity).getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        val name = "Departure Notification"
        val description = "A notification informing the user that it's time to leave"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = description
        val notificationManager = (activity as PrimaryActivity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}