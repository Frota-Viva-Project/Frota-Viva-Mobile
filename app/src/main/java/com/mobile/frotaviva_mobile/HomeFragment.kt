package com.mobile.frotaviva_mobile.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.MainActivity
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding
import java.io.IOException
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.mobile.frotaviva_mobile.MapDialogFragment

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PIN_SIZE_DP = 64

    private val isPermissionGranted
        get() = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                setupMapAndLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                setupMapAndLocation()
            }
            else -> {
                Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.cardMapContainer.setOnClickListener {
            openFullScreenMap()
        }

        binding.updateButton.setOnClickListener {
            getLastKnownLocationAndUpdateUI()
        }

        binding.mapClickOverlay.setOnClickListener {
            Log.d("HomeFragment", "Overlay Clicado! Abrindo modal.")
            Toast.makeText(requireContext(), "Abrindo Mapa...", Toast.LENGTH_SHORT).show()
            openFullScreenMap()
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener {
                fetchTruckIdAndSetInActivity(user.uid)
            }.addOnFailureListener {
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        vectorDrawable.setBounds(0, 0, PIN_SIZE_DP, PIN_SIZE_DP)
        val bitmap = createBitmap(PIN_SIZE_DP, PIN_SIZE_DP)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun requestLocationPermissions() {
        if (isPermissionGranted) {
            setupMapAndLocation()
            getLastKnownLocationAndUpdateUI()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Alteração: Desabilita TODOS os gestos. O mapa agora é uma imagem estática.
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false

        requestLocationPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun openFullScreenMap() {
        if (!isPermissionGranted) {
            Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                // NOVO: Usa o DialogFragment para o efeito modal
                val mapDialog = MapDialogFragment.newInstance(it.latitude, it.longitude)
                mapDialog.show(childFragmentManager, MapDialogFragment.TAG)

            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupMapAndLocation() {
        if (!isPermissionGranted) {
            return
        }

        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)

                    val truckIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.home_pin)

                    googleMap.clear()

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.marker_title))
                            .icon(truckIcon)
                    )
                }
            }
            .addOnFailureListener {
            }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocationAndUpdateUI() {
        if (!isPermissionGranted) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getCityFromLocation(it)
                    setupMapAndLocation()
                } ?: run {
                    Toast.makeText(requireContext(), getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.location_failed), Toast.LENGTH_SHORT).show()
            }
    }

    @Suppress("DEPRECATION")
    private fun getCityFromLocation(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))

        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea
                val state = address.adminArea ?: ""

                binding.approximateLocation.text = String.format(getString(R.string.currently_at), "$city, $state")
            } else {
                binding.approximateLocation.text = getString(R.string.location_unknown)
            }
        } catch (_: IOException) {
            binding.approximateLocation.text = getString(R.string.service_error)
        } catch (_: Exception) {
            binding.approximateLocation.text = getString(R.string.unknown_error)
        }
    }

    private fun fetchTruckIdAndSetInActivity(uid: String) {
        db.collection("driver").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val truckId = document.getLong("truckId")?.toInt()

                    if (truckId != null && truckId > 0) {
                        (activity as? MainActivity)?.truckId = truckId

                        val currentItemId = (activity as? MainActivity)?.binding?.navbarInclude?.bottomNavigation?.selectedItemId

                        when (currentItemId) {
                            R.id.nav_manutencoes -> {
                                if (currentItemId != R.id.nav_home) {
                                    (activity as? MainActivity)?.navigateToMaintenance()
                                }
                            }
                            R.id.nav_avisos -> {
                                if (currentItemId != R.id.nav_home) {
                                    (activity as? MainActivity)?.navigateToAlerts()
                                }
                            }
                        }
                    } else {
                        (activity as? MainActivity)?.truckId = null
                    }
                }
            }
            .addOnFailureListener {
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}