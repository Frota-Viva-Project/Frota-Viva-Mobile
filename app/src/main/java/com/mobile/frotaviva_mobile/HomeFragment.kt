package com.mobile.frotaviva_mobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.mobile.frotaviva_mobile.MapDialogFragment
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding
import com.mobile.frotaviva_mobile.fragments.RoutesDialogFragment
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

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

        binding.updateButton.setOnClickListener {
            getLastKnownLocationAndUpdateUI()
        }

        binding.buttonSeeMore.setOnClickListener {
            val truckId = (activity as? MainActivity)?.truckId

            if (truckId != null && truckId > 0) {
                openAllRoutesModal(truckId)
            } else {
                Toast.makeText(requireContext(), "Aguarde, carregando ID do caminhão...", Toast.LENGTH_SHORT).show()
                // Você pode tentar buscar o ID do caminhão novamente aqui se necessário.
            }
        }

        binding.mapClickOverlay.setOnClickListener {
            openFullScreenMap()
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener {
                fetchDriverAndTruckDetails(user.uid)
            }
        } else {
            // Lidar com usuário não autenticado
            updateDriverDetailsDisplay(
                getString(R.string.unauthenticated),
                getString(R.string.unauthenticated),
                getString(R.string.unauthenticated)
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return

        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.mapClickOverlay.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.routeDeparture.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.routeArrival.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun fetchRoutes(truckId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getRoutes(truckId)

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val activeRoute = response.body()
                        ?.firstOrNull { it.status == "EM ROTA" }

                    if (activeRoute != null) {
                        updateRouteDisplay(activeRoute.destinoInicial, activeRoute.destinoFinal)
                    } else {
                        updateRouteDisplay(getString(R.string.no_active_route), getString(R.string.no_active_route))
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar rotas: ${response.code()}", Toast.LENGTH_LONG).show()
                    updateRouteDisplay(getString(R.string.loading_error), getString(R.string.loading_error))
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão/API", Toast.LENGTH_LONG).show()
                }
                updateRouteDisplay(getString(R.string.loading_error), getString(R.string.loading_error))
            }
        }
    }

    private fun fetchMeters(truckId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMeters(truckId)

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val meterData = response.body()

                    if (meterData != null) {
                        updateMetersDisplay(
                            meterData.nivelCombustivel,
                            meterData.cargaMotor,
                            meterData.velocidadeVeiculo.toInt()
                        )
                    } else {
                        updateMetersDisplay(0,0,0)
                        Toast.makeText(requireContext(), "Dados dos medidores não retornaram",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar medidores: ${response.code()}", Toast.LENGTH_LONG).show()
                    updateMetersDisplay(0,0,0)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e("API_CATCH", "Erro de conexão/API: ${e.message}", e)
                    Toast.makeText(requireContext(), "Erro de conexão/API", Toast.LENGTH_LONG).show()
                }
                updateMetersDisplay(0,0,0)
            }
        }
    }

    private fun updateRouteDisplay(departure: String, arrival: String) {
        if (_binding == null) return
        binding.routeDeparture.text = departure
        binding.routeArrival.text = arrival
    }


    private fun updateMetersDisplay(
        fuelLevel: Int,
        loadMotor: Int,
        speed: Int
    ) {
        if (_binding == null) return

        binding.progressBarFuel.progress = fuelLevel
        binding.fuelStatus.text = "$fuelLevel% / 100"

        binding.progressBarLoadMotor.progress = loadMotor
        binding.loadMotorStatus.text = "$loadMotor% / 100"

        binding.progressBarSpeed.progress = speed
        binding.speedStats.text = "$speed% / 200"
    }

    private fun updateDriverDetailsDisplay(name: String, carModel: String, carPlate: String) {
        if (_binding == null) return
        binding.collaboratorName.text = name
        binding.collaboratorCar.text = carModel
        binding.collaboratorPlate.text = carPlate
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
                val mapDialog = MapDialogFragment.newInstance(it.latitude, it.longitude)
                mapDialog.show(childFragmentManager, MapDialogFragment.TAG)
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAllRoutesModal(truckId: Int) {
        val dialog = RoutesDialogFragment.newInstance(truckId)
        dialog.show(childFragmentManager, RoutesDialogFragment.TAG)
    }

    private fun fetchDriverAndTruckDetails(uid: String) {
        showLoading(true)

        db.collection("driver").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                showLoading(false)

                if (document.exists()) {
                    val name = document.getString("name") ?: getString(R.string.data_not_found)
                    val carModel = document.getString("carModel") ?: getString(R.string.data_not_found)
                    val carPlate = document.getString("carPlate") ?: getString(R.string.data_not_found)

                    val truckId = document.getLong("truckId")?.toInt()
                    if (truckId != null && truckId > 0) {
                        (activity as? MainActivity)?.truckId = truckId
                        fetchRoutes(truckId)
                        fetchMeters(truckId)
                    }

                    updateDriverDetailsDisplay(name, carModel, carPlate)

                } else {
                    updateDriverDetailsDisplay(
                        getString(R.string.loading_error),
                        getString(R.string.loading_error),
                        getString(R.string.loading_error)
                    )
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showLoading(false)

                updateDriverDetailsDisplay(
                    getString(R.string.loading_error),
                    getString(R.string.loading_error),
                    getString(R.string.loading_error)
                )
                Log.e("HomeFragment", "Falha ao buscar detalhes do motorista", it)
            }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupMapAndLocation() {
        if (!isPermissionGranted) return

        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastKnownLocationAndUpdateUI() {
        if (!isPermissionGranted) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                getCityFromLocation(it)
                setupMapAndLocation()
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.location_not_available), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}