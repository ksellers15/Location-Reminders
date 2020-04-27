package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.CameraUpdateFactory.newLatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback  {

    val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 4608

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    lateinit var map: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var locationPermissionGranted: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFrag = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrag.getMapAsync(this)

//        COMPLETED: add the map setup implementation
//        COMPLETED: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if(checkSelfPermission(this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateMapSettings()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        locationPermissionGranted = false
        if(requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
            }
        }

        updateMapSettings()
    }


   private fun updateMapSettings() {
       if (locationPermissionGranted) {
           map.isMyLocationEnabled = true
           map.uiSettings.apply {
               isZoomControlsEnabled = true
               isZoomGesturesEnabled= true
               isMyLocationButtonEnabled = true
               isMapToolbarEnabled = true
           }


           // Fetch to and move camera to user's location. Also zoom in
           fusedLocationClient.lastLocation.addOnCompleteListener() { task ->
               if(task.isSuccessful) {
                   val loc = task.result!!
                   map.moveCamera(CameraUpdateFactory.newCameraPosition(
                       CameraPosition.fromLatLngZoom(LatLng(loc.latitude, loc.longitude), map.maxZoomLevel/1.5f)
                   ))
               }
           }

           map.setOnMapLongClickListener {
               latLng ->
               map.addMarker(
                   MarkerOptions()
                   .position(latLng)
                   .title(getString(R.string.dropped_pin))
               )
               // Move camera to user's location
               map.moveCamera(newLatLng(latLng))
           }
       } else {
           map.isMyLocationEnabled = false
           map.uiSettings.apply {
               isZoomControlsEnabled = false
               isZoomGesturesEnabled= false
               isMyLocationButtonEnabled = false
               isMapToolbarEnabled = true
           }
           getLocationPermission()
       }
   }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!

        getLocationPermission()
    }


}
