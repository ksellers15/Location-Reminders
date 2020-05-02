package com.udacity.project4.locationreminders.savereminder

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            COMPLETED: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            if(!title.isNullOrEmpty() && !description.isNullOrEmpty() && !location.isNullOrEmpty()) {
                // Create reminder and save it in view model
                val reminder = ReminderDataItem(title, description, location, latitude, longitude)
                _viewModel.saveReminder(reminder)

                // Create Geofencing Request an trigger it based on entering the reminder location
                // at a radius of default_geo_radius = 3.0
                GeofencingRequest.Builder()
                    .addGeofence(buildGeoFence(reminder))
                    .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val pendIntent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
                PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    pendIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }

    private fun buildGeoFence(reminder: ReminderDataItem): Geofence {
        return Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(reminder.latitude!!, reminder.longitude!!, 3.0f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}