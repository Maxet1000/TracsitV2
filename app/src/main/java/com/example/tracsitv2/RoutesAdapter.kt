package com.example.tracsitv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracsitv2.data.Event

class RoutesAdapter (private val listener: Communicator) : RecyclerView.Adapter<RoutesAdapter.RoutesViewHolder>(){

    private var eventList = emptyList<Event>()

    inner class RoutesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val startLocation: TextView = itemView.findViewById(R.id.tvEventName)
        val endLocation: TextView = itemView.findViewById(R.id.tvEventPeriod)
        val departureTime: TextView = itemView.findViewById(R.id.tvDepartureTime)
        val arrivalTime: TextView = itemView.findViewById(R.id.tvArrivalTime)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route, parent, false)
        return RoutesViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        holder.startLocation.text = eventList[position].startLocation
        holder.endLocation.text = eventList[position].endLocation
        holder.departureTime.text = MainActivity.convertTimeToHHmmString(eventList[position].commuteStartTime)
        holder.arrivalTime.text = MainActivity.convertTimeToHHmmString(eventList[position].commuteEndTime)
    }

    override fun getItemCount() = eventList.size

    fun setData(event: List<Event>) {
        this.eventList = event
        notifyDataSetChanged()
    }

}