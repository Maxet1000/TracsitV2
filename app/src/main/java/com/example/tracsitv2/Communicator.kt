package com.example.tracsitv2

interface Communicator {
    fun passData(databaseId: Int, name: String, startLocation: String, endLocation: String, eventStartTime: Long, eventEndTime: Long, commuteStartTime: Long, commuteEndTime: Long)
}