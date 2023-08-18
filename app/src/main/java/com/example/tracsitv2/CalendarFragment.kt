package com.example.tracsitv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracsitv2.data.EventViewModel
import com.example.tracsitv2.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment(R.layout.fragment_calendar), Communicator {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var eventViewModel: EventViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarBinding.bind(view)

        // Recyclerview
        val adapter = EventsAdapter(this@CalendarFragment)
        binding.rvEvents.adapter = adapter
        binding.rvEvents.layoutManager = LinearLayoutManager(requireActivity())

        //EventViewModel
        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        eventViewModel.getAllEventsOrderedByEventStartTime.observe(viewLifecycleOwner,
            Observer { event ->
                adapter.setData(event)
            })
    }

    override fun passData(
        databaseId: Int,
        name: String,
        startLocation: String,
        endLocation: String,
        eventStartTime: Long,
        eventEndTime: Long,
        commuteStartTime: Long,
        commuteEndTime: Long
    ) {
        (activity as PrimaryActivity).addFragmentToHistory(R.id.bnbNotifications)

        val bundle = Bundle()
        bundle.putInt("databaseId", databaseId)
        bundle.putString("name", name)
        bundle.putString("startLocation", startLocation)
        bundle.putString("endLocation", endLocation)
        bundle.putLong("eventStartTime", eventStartTime)
        bundle.putLong("eventEndTime", eventEndTime)
        bundle.putLong("commuteStartTime", commuteStartTime)
        bundle.putLong("commuteEndTime", commuteEndTime)

        val transaction = this.parentFragmentManager.beginTransaction()
        val detailsFragment = DetailsFragment()
        detailsFragment.arguments = bundle
        transaction.replace(R.id.flFragment, detailsFragment)
        transaction.commit()    }
}

