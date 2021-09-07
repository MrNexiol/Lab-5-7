package com.prograils.lab5_7

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.prograils.lab5_7.databinding.ActivityMapsBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.N)
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, OnNmeaMessageListener, GoogleMap.OnMapClickListener {

    private var mMap: GoogleMap? = null
    private var _binding: ActivityMapsBinding? = null
    private val binding get() = _binding!!
    private var measuring = false
    private var chosenLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null
    private var distanceBetweenMarkers = 0.0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapsBinding.inflate(layoutInflater)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0F, this)
        locationManager.addNmeaListener(this, null)

        binding.measureDistance.setOnClickListener {
            measuring = !measuring
        }

        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maps_activity_top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (binding.root.isDrawerOpen(GravityCompat.END)) {
            binding.root.closeDrawer(GravityCompat.END)
        } else {
            binding.root.openDrawer(GravityCompat.END)
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setOnMapClickListener(this)
    }

    override fun onLocationChanged(location: Location) {
        if (currentLocationMarker == null) {
            val currentLocation = LatLng(location.latitude, location.longitude)
            currentLocationMarker = mMap?.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10F))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onNmeaMessage(message: String?, timestamp: Long) {
        message?.let {
            val values = message.split(',')
            val tmp = values[0].subSequence(3,6)
            if (tmp == "GGA") {
                binding.szerokoscValue.text = "${values[2]} ${values[3]}"
                binding.dlugoscValue.text = "${values[4]} ${values[5]}"
                binding.wysokoscValue.text = "${values[9]} ${values[10]}"
                binding.satelityValue.text = values[7]
                binding.hdopValue.text = values[8]
            } else {
                binding.szerokoscValue.text = "${values[3]} ${values[4]}"
                binding.dlugoscValue.text = "${values[5]} ${values[6]}"
                binding.predkoscValue.text = values[7]
                binding.kursValue.text = values[8]
            }
        }
    }

    override fun onMapClick(p0: LatLng) {
        if (measuring) {
            chosenLocationMarker?.remove()
            chosenLocationMarker = mMap?.addMarker(MarkerOptions().position(p0).title("Chosen location"))
            currentPolyline?.remove()
            val polylineOptions = PolylineOptions().add(currentLocationMarker!!.position).add(chosenLocationMarker!!.position)
            currentPolyline = mMap?.addPolyline(polylineOptions)
            distanceBetweenMarkers = calculateDistance(currentLocationMarker!!.position, chosenLocationMarker!!.position)
            distanceBetweenMarkers = String.format("%.1f", distanceBetweenMarkers).toDouble()
            binding.distanceValue.text = distanceBetweenMarkers.toString() + " KM"
            binding.distanceValue.visibility = View.VISIBLE
            measuring = false
        }
    }

    private fun calculateDistance(p0: LatLng, p1: LatLng): Double {
        val expressionConst = 40075.704/360
        val tmp1 = (p1.latitude - p0.latitude).pow(2)
        val tmp2 = (p0.latitude * PI)/180
        val tmp3 = cos(tmp2)
        val tmp4 = p1.longitude - p0.longitude
        val tmp5 = tmp3 * tmp4
        val tmp6 = tmp5.pow(2)
        val tmp7 = tmp1 + tmp6
        val tmp8 = sqrt(tmp7)
        return tmp8 * expressionConst
    }
}