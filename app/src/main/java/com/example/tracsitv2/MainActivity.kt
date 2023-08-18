package com.example.tracsitv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tracsitv2.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var homeAddress : String
    private lateinit var workAddress : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("Pref", Context.MODE_PRIVATE)
        homeAddress = sharedPreferences?.getString("homeAddress", "").toString()
        workAddress = sharedPreferences?.getString("workAddress", "").toString()

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            if (homeAddress == "" || workAddress == ""){
              Toast.makeText(this, "Please Register Before Logging In", Toast.LENGTH_SHORT).show()
            } else startActivity(Intent(this, PrimaryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("Pref", Context.MODE_PRIVATE)
        homeAddress = sharedPreferences?.getString("homeAddress", "").toString()
        workAddress = sharedPreferences?.getString("workAddress", "").toString()
    }

    companion object {
        fun convertTimeToHHmmString(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("HH'h'mm", Locale.FRANCE)
            return format.format(date)
        }

        fun convertTimeHHmmStringToLong(timeString: String, sameDayRefTime: Long): Long {
            val daysSince1970 = sameDayRefTime / (1000 * 60 * 60 * 24)
            val currentDayStartTime = daysSince1970 * 1000 * 60 * 60 * 24
            val timeSinceStartDay =
                (timeString.substring(0, 2).toLong() - 2) * 60 * 60 * 1000 + timeString.substring(3, 5)
                    .toLong() * 60 * 1000
            return currentDayStartTime + timeSinceStartDay
        }
    }
}