package com.example.tracsitv2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "event_data")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    var startLocation: String,
    var endLocation: String,
    val eventStartTime: Long,
    val eventEndTime: Long,
    var commuteStartTime: Long,
    var commuteEndTime: Long
)
