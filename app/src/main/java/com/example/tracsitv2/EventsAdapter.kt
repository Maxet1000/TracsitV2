package com.example.tracsitv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsitv2.data.Event

class EventsAdapter (private val listener: Communicator) : RecyclerView.Adapter<EventsAdapter.EventsViewHolder>(){

    private var eventList = emptyList<Event>()

    inner class EventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tvEventName)
        val period: TextView = itemView.findViewById(R.id.tvEventPeriod)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                val clickedEvent = eventList[position]
                if (position != RecyclerView.NO_POSITION) {
                    listener.passData(clickedEvent.id, clickedEvent.name,
                        clickedEvent.startLocation, clickedEvent.endLocation,
                        clickedEvent.eventStartTime, clickedEvent.eventEndTime, clickedEvent.commuteStartTime,
                        clickedEvent.commuteEndTime)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val periodText = "From" + MainActivity.convertTimeToHHmmString(eventList[position].eventStartTime) + "to" + MainActivity.convertTimeToHHmmString(eventList[position].eventEndTime)

        holder.name.text = eventList[position].name
        holder.period.text = periodText
    }

    override fun getItemCount() = eventList.size

    fun setData(event: List<Event>) {
        this.eventList = event
        notifyDataSetChanged()
    }

}