package com.example.tracsitv2

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tracsitv2.data.Event
import com.example.tracsitv2.data.EventViewModel
import com.example.tracsitv2.databinding.FragmentDetailsBinding
import com.example.tracsitv2.mapsApi.RetrofitInstance
import com.example.tracsitv2.notifications.*
import com.example.tracsitv2.notifications.Notification
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var eventViewModel: EventViewModel

    private var databaseId: Int = 0
    lateinit var event: Event

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailsBinding.bind(view)
        databaseId = arguments?.getInt("databaseId") ?: 0
        val name = arguments?.getString("name") ?: ""
        val startLocation = arguments?.getString("startLocation") ?: ""
        val endLocation = arguments?.getString("endLocation") ?: ""
        val eventStartTime = arguments?.getLong("eventStartTime") ?: 0
        val eventEndTime = arguments?.getLong("eventEndTime") ?: 0
        var commuteStartTime = arguments?.getLong("commuteStartTime") ?: 0
        val commuteEndTime = arguments?.getLong("commuteEndTime") ?: 0


        binding.tvName.text = name
        binding.etStartLocation.setText(startLocation)
        binding.etEndLocation.setText(endLocation)

        val eventTimeString = "From" + MainActivity.convertTimeToHHmmString(eventStartTime) + "to" + MainActivity.convertTimeToHHmmString(eventEndTime)

        binding.tvEventTime.text = eventTimeString

        if (commuteStartTime != 0L) {
            binding.etDepartureTime.setText(MainActivity.convertTimeToHHmmString(commuteStartTime))
        } else binding.etDepartureTime.setText("")
        if (commuteEndTime != 0L) {
            binding.etArrivalTime.setText(MainActivity.convertTimeToHHmmString(commuteEndTime))
        } else binding.etArrivalTime.setText("")

        if (databaseId != 0) {
            event = Event(databaseId, name, startLocation, endLocation, eventStartTime, eventEndTime, commuteStartTime, commuteEndTime)
        }
        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        createNotificationChannel()

        binding.btnUpdate.setOnClickListener {
            upsertEvent(eventStartTime, eventEndTime)
            commuteStartTime = MainActivity.convertTimeHHmmStringToLong(binding.etDepartureTime.text.toString(), eventStartTime)
            if (binding.etDepartureTime.text.toString() != "" && binding.etArrivalTime.text.toString() != "") {
                scheduleNotification(commuteStartTime - 1000 * 60 * 60)
            }
            (activity as PrimaryActivity).onBackPressed()
        }

        binding.btnDelete.setOnClickListener {
            deleteEvent(event)
        }

        binding.btnApi.setOnClickListener {
            val apiKey = BuildConfig.API_KEY
            val checkedRadioButtonId = binding.rgDetails.checkedRadioButtonId
            val mode = binding.rgDetails.findViewById<RadioButton>(checkedRadioButtonId).text.toString()

            val origin = binding.etStartLocation.text.toString()
            val destination = binding.etEndLocation.text.toString()
            if (mode != "transit") {
                lifecycleScope.launchWhenCreated {
                    val response = try {
                        RetrofitInstance.api.getTravelTime(
                            key = apiKey,
                            destinations = destination,
                            origins = origin,
                            units = "metric",
                            mode = mode
                        )
                    } catch (e: IOException) {
                        Log.e("mapsAPI", "IOException, no internet connection available")
                        return@launchWhenCreated
                    } catch (e: HttpException) {
                        Log.e("mapsAPI", "HttpException, unexpected response")
                        return@launchWhenCreated
                    }
                    if (response.isSuccessful && response.body() != null) {
                        val travelData = response.body()
                        if (travelData?.status == "OK") {
                            if (travelData.rows[0].elements[0].status == "OK") {
                                val travelDurationSeconds =
                                    travelData.rows[0].elements[0].duration.value
                                val travelDurationMinutes = travelDurationSeconds / 60 + 1
                                binding.tvTravelTime.text = getString(
                                    R.string.commuteTimeExplanation,
                                    travelDurationMinutes
                                )
                                val arrivalTimeString =
                                    MainActivity.convertTimeToHHmmString(eventStartTime)
                                val departureTimeString =
                                    MainActivity.convertTimeToHHmmString(eventStartTime - 1000 * travelDurationSeconds)
                                binding.etArrivalTime.setText(arrivalTimeString)
                                binding.etDepartureTime.setText(departureTimeString)
                            } else Toast.makeText(
                                requireContext(),
                                "Address not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else Toast.makeText(
                            requireContext(),
                            "Please enter two addresses",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("mapsAPI", "Response not successful")
                    }
                }
            } else {
                lifecycleScope.launchWhenCreated {
                    val response = try {
                        RetrofitInstance.api.getTransitData(
                            key = apiKey,
                            destinations = destination,
                            origins = origin,
                            units = "metric",
                            mode = mode,
                            arrival_time = eventStartTime/1000
                        )
                    } catch (e: IOException) {
                        Log.e("mapsAPI", "IOException, no internet connection available")
                        return@launchWhenCreated
                    } catch (e: HttpException) {
                        Log.e("mapsAPI", "HttpException, unexpected response")
                        return@launchWhenCreated
                    }
                    if (response.isSuccessful && response.body() != null) {
                        val travelData = response.body()
                        if (travelData?.status == "OK") {
                            if (travelData.geocoded_waypoints[0].geocoder_status == "OK") {
                                val departureTimeSeconds = travelData.routes[0].legs[0].departure_time.value
                                val arrivalTimeSeconds = travelData.routes[0].legs[0].arrival_time.value
                                val travelDurationSeconds = arrivalTimeSeconds - departureTimeSeconds
                                val travelDurationMinutes = travelDurationSeconds/60
                                binding.tvTravelTime.text = getString(
                                    R.string.commuteTimeExplanation,
                                    travelDurationMinutes
                                )
                                val arrivalTimeString =
                                    MainActivity.convertTimeToHHmmString(arrivalTimeSeconds*1000L)
                                val departureTimeString =
                                    MainActivity.convertTimeToHHmmString(departureTimeSeconds*1000L)
                                binding.etArrivalTime.setText(arrivalTimeString)
                                binding.etDepartureTime.setText(departureTimeString)
                            } else Toast.makeText(
                                requireContext(),
                                "Address not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else Toast.makeText(
                            requireContext(),
                            "Please enter two addresses",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("mapsAPI", "Response not successful")
                    }
                }
            }
        }
    }

    private fun scheduleNotification(time: Long) {
        val intent = Intent((activity as PrimaryActivity).applicationContext, Notification::class.java)
        val title = binding.tvName.text.toString()
        val message = binding.etDepartureTime.text.toString()
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

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
        val timeString = MainActivity.convertTimeToHHmmString(time)
        Toast.makeText(requireContext(), "Notification Set At $timeString", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val name = "Departure Notification"
        val description = "A notification informing the user that it's time to leave"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = description
        val notificationManager = (activity as PrimaryActivity).getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun upsertEvent(eventStartTime: Long, eventEndTime: Long) {
        val name = binding.tvName.text.toString()
        val startLocation = binding.etStartLocation.text.toString()
        val endLocation = binding.etEndLocation.text.toString()

        var commuteStartTime: Long = 0
        if (binding.etDepartureTime.text.toString() != "") {
            commuteStartTime = MainActivity.convertTimeHHmmStringToLong(binding.etDepartureTime.text.toString(), eventStartTime)
        }

        var commuteEndTime: Long = 0
        if (binding.etArrivalTime.text.toString() != "") {
            commuteEndTime = MainActivity.convertTimeHHmmStringToLong(binding.etArrivalTime.text.toString(), eventStartTime)
        }

        if (inputCheck(name, startLocation, endLocation, commuteStartTime, commuteEndTime)) {
            val event = Event(
                databaseId,
                name,
                startLocation,
                endLocation,
                eventStartTime,
                eventEndTime,
                commuteStartTime,
                commuteEndTime
            )
            eventViewModel.upsertEvent(event)
            Toast.makeText(requireContext(), "Database updated", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(requireContext(), "Some Fields are still empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEvent(event: Event) {
        val name = binding.tvName.text.toString()
        if (name != "") {
            val builder = AlertDialog.Builder(requireContext())
            builder.setPositiveButton("Yes") { _, _ ->
                eventViewModel.deleteEvent(event)
                (activity as PrimaryActivity).onBackPressed()
            }
            builder.setNegativeButton("No") { _, _ -> }
            builder.setTitle("Delete ${name}?")
            builder.setMessage("Are you sure you want to delete ${name}?")
            builder.create().show()
        }
    }

    private fun inputCheck(name: String, startLocation: String, endLocation: String, commuteStartTime: Long, commuteEndTime: Long): Boolean {
        return !(TextUtils.isEmpty(name) && TextUtils.isEmpty(startLocation) && TextUtils.isEmpty(endLocation) && commuteStartTime == 0L && commuteEndTime == 0L)
    }

}