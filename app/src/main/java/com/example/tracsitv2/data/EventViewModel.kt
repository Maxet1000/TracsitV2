package com.example.tracsitv2.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventViewModel(application: Application): AndroidViewModel(application) {

    val getAllEventsOrderedByEventStartTime: LiveData<List<Event>>
    val getAllEventsWithCommute: LiveData<List<Event>>
    private val repository: EventRepository

    init {
        val eventDao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        getAllEventsOrderedByEventStartTime = repository.getAllEventsOrderedByEventStartTime
        getAllEventsWithCommute = repository.getAllEventsWithCommute
    }

    fun upsertEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }
}