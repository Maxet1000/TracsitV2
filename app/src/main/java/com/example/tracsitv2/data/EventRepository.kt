package com.example.tracsitv2.data

import androidx.lifecycle.LiveData

class EventRepository(private val eventDao: EventDao) {

    val getAllEventsOrderedByEventStartTime: LiveData<List<Event>> = eventDao.getAllEventsOrderedByEventStartTime()

    val getAllEventsWithCommute: LiveData<List<Event>> = eventDao.getAllEventsWithCommute()

    suspend fun upsertEvent(event: Event) {
        eventDao.upsertEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }
}