package com.example.tracsitv2.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface EventDao {

    @Upsert
    suspend fun upsertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("Select * FROM event_data ORDER BY eventStartTime ASC")
    fun getAllEventsOrderedByEventStartTime(): LiveData<List<Event>>

    @Query("Select * FROM event_data WHERE commuteStartTime <> 0 ORDER BY eventStartTime ASC")
    fun getAllEventsWithCommute(): LiveData<List<Event>>
}