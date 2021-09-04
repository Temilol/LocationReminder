package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    private lateinit var map: GoogleMap
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocation()
        binding.selectButton.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        try{
            if (permissionGranted()) {
                val lastLocation = fusedLocationProviderClient.lastLocation
                lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val lastKnownLocation = task.result!!
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude
                                ), ZOOM
                            )
                        )
                    } else {
                        val latitude = 37.422160
                        val longitude = -122.084270
                        val location = LatLng(latitude, longitude)
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(location, ZOOM)
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun permissionGranted () : Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        enableMyLocation()
        setMapClick(map)
        setPoiClick(map)

    }

    private fun setMapClick(googleMap: GoogleMap) {
//        TODO: put a marker to location that the user selected
        googleMap.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            _viewModel.selectedPOI.postValue(null)
            _viewModel.latitude.postValue(latLng.latitude)
            _viewModel.longitude.postValue(latLng.longitude)
            _viewModel.reminderSelectedLocationStr.postValue(getString(R.string.dropped_pin))
            binding.selectButton.visibility = View.VISIBLE
        }
    }

    private fun setPoiClick(googleMap: GoogleMap) {
        googleMap.setOnPoiClickListener { poi ->
            map.clear()
            _viewModel.selectedPOI.postValue(poi)
            _viewModel.latitude.postValue(poi.latLng.latitude)
            _viewModel.longitude.postValue(poi.latLng.longitude)
            _viewModel.reminderSelectedLocationStr.postValue(poi?.name)
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            binding.selectButton.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!permissionGranted()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            map.isMyLocationEnabled = false
            return
        }
        map.isMyLocationEnabled = true
    }

    private fun setMapStyle(map: GoogleMap) {
//      TODO: add style to the map
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.location_required_error),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    companion object {
        const val TAG = "SelectLocationFragment"
        const val REQUEST_LOCATION_PERMISSION = 1
        const val ZOOM = 15f
    }
}
