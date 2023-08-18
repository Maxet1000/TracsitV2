package com.example.tracsitv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracsitv2.data.EventViewModel
import com.example.tracsitv2.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment(R.layout.fragment_notifications), Communicator {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var eventViewModel: EventViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)

        // Recyclerview
        val adapter = RoutesAdapter(this@NotificationsFragment)
        binding.rvTrips.adapter = adapter
        binding.rvTrips.layoutManager = LinearLayoutManager(requireActivity())

        //EventViewModel
        eventViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        eventViewModel.getAllEventsWithCommute.observe(viewLifecycleOwner, Observer { event ->
            adapter.setData(event)
        })

        binding.btnNewTrip.setOnClickListener {
            val transaction = this.parentFragmentManager.beginTransaction()
            val detailsFragment = DetailsFragment()
            transaction.replace(R.id.flFragment, detailsFragment)
            transaction.commit()
        }
    }

    override fun passData(databaseId: Int, name: String, startLocation: String, endLocation: String, eventStartTime: Long, eventEndTime: Long, commuteStartTime: Long, commuteEndTime: Long) {
        (activity as PrimaryActivity).addFragmentToHistory(R.id.bnbHome)

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
        transaction.commit()
    }
}