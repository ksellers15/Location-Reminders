package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnPoiClickListener, GoogleMap.OnMapClickListener/*, GoogleMap.OnMapLongClickListener*/ {

    val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 4608

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    lateinit var map: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var selectedLocation: Marker? = null

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
//        COMPLETED: put a marker to location that the user selected


//        COMPLETED: call this function after the user confirms on the selected location

        return binding.root
    }

    private fun onLocationSelected() {
        if(selectedLocation == null) {
            Toast.makeText(requireContext(), "You have not selected a point of interest.", Toast.LENGTH_SHORT).show()
            return
        }

        val poi = PointOfInterest(selectedLocation!!.position, selectedLocation!!.id, selectedLocation!!.title)
        _viewModel.setSelectedLocation(poi)
        _viewModel.navigationCommand.value = NavigationCommand.Back

        //        COMPLETED: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // COMPLETED: Change the map type based on the user's selection.
        R.id.action_save_location -> {
            onLocationSelected()
            true
        }
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
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
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
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
           
           // If there is already a place selected. Reload that place,
           // if not, show the user's current location
           if(!setInitialMarker()){
               moveCameraToUserPosition()
            }

           map.setOnPoiClickListener(this)
//           map.setOnMapLongClickListener(this)
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

    private fun moveCameraToUserPosition() {
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val loc = task.result!!
                map.animateCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(LatLng(loc.latitude, loc.longitude), map.maxZoomLevel/1.5f)
                ))
//                   selectedLocation = map.addMarker(MarkerOptions()
//                       .position(LatLng(loc.latitude, loc.longitude))
//                       .title(getString(R.string.your_location))
//                       .snippet("${loc.latitude}, ${loc.longitude}"))
//
//                   selectedLocation.showInfoWindow()
            }
        }
    }

    // Set the selected marker to the point of interest currently stored in view model
    private fun setInitialMarker(): Boolean {
        val prevSelectedLoc = _viewModel.selectedPOI.value
        if(prevSelectedLoc != null && prevSelectedLoc.name.isNotEmpty()) {
            selectedLocation = map.addMarker(MarkerOptions()
                .title(prevSelectedLoc.name)
                .position(prevSelectedLoc.latLng)
                .snippet("${prevSelectedLoc.latLng.latitude}, ${prevSelectedLoc.latLng.longitude}"))

            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(prevSelectedLoc.latLng, map.maxZoomLevel/1.25f)
            ))
            return true
        }
        
        return false
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!

        getLocationPermission()
    }

    override fun onPoiClick(pointOfInterest: PointOfInterest?) {
        if(selectedLocation != null)
            selectedLocation!!.remove()

        selectedLocation = map.addMarker(
            MarkerOptions()
                .position(pointOfInterest!!.latLng)
                .title(pointOfInterest.name)
                .snippet("${pointOfInterest.latLng.latitude}, ${pointOfInterest.latLng.longitude}")
        )

        selectedLocation!!.showInfoWindow()

    }

    // Used to cancel location selection
    override fun onMapClick(p0: LatLng?) {
        if(selectedLocation != null && selectedLocation!!.position != p0) {
            map.clear()
            selectedLocation = null
        }
    }

//    override fun onMapLongClick(latLng: LatLng?) {
//        selectedLocation.remove()
//        selectedLocation = map.addMarker(MarkerOptions()
//            .position(latLng!!)
//            .title(getString(R.string.dropped_pin))
//            .snippet("${latLng.latitude}, ${latLng.longitude}"))
//
//        selectedLocation.showInfoWindow()
//    }


}
