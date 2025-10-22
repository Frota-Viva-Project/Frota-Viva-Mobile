package com.mobile.frotaviva_mobile // Use o seu pacote correto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapDialogFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var startLat: Double = 0.0
    private var startLng: Double = 0.0

    companion object {
        const val TAG = "MapDialogFragment"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"

        fun newInstance(lat: Double, lng: Double): MapDialogFragment {
            val fragment = MapDialogFragment()
            val args = Bundle().apply {
                putDouble(EXTRA_LAT, lat)
                putDouble(EXTRA_LNG, lng)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o estilo para DialogFragment: sem título, mas com fundo transparente/escurecido
        setStyle(STYLE_NORMAL, R.style.CustomMapDialogTheme)

        arguments?.let {
            startLat = it.getDouble(EXTRA_LAT)
            startLng = it.getDouble(EXTRA_LNG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_map_fullscreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.full_screen_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        val startLocation = LatLng(startLat, startLng)

        googleMap.addMarker(MarkerOptions().position(startLocation).title(getString(R.string.marker_title)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 16f))
    }
}