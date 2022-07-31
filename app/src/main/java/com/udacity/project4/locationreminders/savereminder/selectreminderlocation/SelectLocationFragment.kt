package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private var poiPoint: PointOfInterest? = null

    private var latLng: LatLng? = null

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var map: GoogleMap

    private val TAG = SelectLocationFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapFragment.getMapAsync(this)

        binding.chooseLocationButton.setOnClickListener{
            onLocationSelected()
        }
        return binding.root
    }

    private fun onLocationSelected() {
        if (poiPoint!= null){
            _viewModel.latitude.value = poiPoint!!.latLng.latitude
            _viewModel.longitude.value = poiPoint!!.latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = poiPoint!!.name
            _viewModel.selectedPOI.value = poiPoint
            findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            return
        }
        if (latLng != null){
            _viewModel.latitude.value = latLng!!.latitude
            _viewModel.longitude.value = latLng!!.longitude
            _viewModel.reminderSelectedLocationStr.value = "Chosen point"
            findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            return

        }
            Toast.makeText(context, "Choose location", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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

        enableMyLocation()

        setMapStyle(map)

        setPoiClick(map)
        setClick(map)
        //testPosition
        val position = LatLng(47.36312971501226, 8.536376953125002)
        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
        map.uiSettings.isMyLocationButtonEnabled = true

    }

    private fun enableMyLocation() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            } === PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION)
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            poiPoint = poi
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }
    }

//    override fun onStop() {
//        super.onStop()
//        poiPoint = null
//        latLng = null
//    }

    private fun setClick(map: GoogleMap) {
        map.setOnMapClickListener { latlng ->
            latLng = latlng
            map.clear()
            val pointMarker = map.addMarker(
                MarkerOptions()
                    .position(latlng)
                    .title("Chosen point")
            )
            pointMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
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

}
