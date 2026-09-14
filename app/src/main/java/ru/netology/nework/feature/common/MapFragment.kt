package ru.netology.nework.feature.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.core.config.AppSecrets
import ru.netology.nework.databinding.FragmentMapBinding
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    @Inject
    lateinit var appSecrets: AppSecrets

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var mapKitInited = false
    private var pickLocation = false
    private var currentLat = 55.751574
    private var currentLng = 37.573856

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            showMap()
        } else {
            binding.mapStubText.visibility = View.VISIBLE
            binding.mapStubText.text = "нужно разрешение на геолокацию"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickLocation = arguments?.getBoolean(ARG_PICK_LOCATION) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapConfirmButton.isVisible = pickLocation
        binding.mapCoordsText.isVisible = pickLocation

        if (appSecrets.mapsApiKey.isBlank()) {
            binding.mapStubText.visibility = View.VISIBLE
            return
        }

        binding.mapConfirmButton.setOnClickListener {
            val result = bundleOf("lat" to currentLat, "lng" to currentLng)
            parentFragmentManager.setFragmentResult("location_pick", result)
            parentFragmentManager.popBackStack()
        }

        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            showMap()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showMap() {
        if (!mapKitInited) {
            MapKitFactory.setApiKey(appSecrets.mapsApiKey)
            MapKitFactory.initialize(requireContext().applicationContext)
            mapKitInited = true
        }

        binding.mapStubText.visibility = View.GONE
        if (mapView == null) {
            mapView = MapView(requireContext())
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            binding.mapContainer.addView(mapView, layoutParams)
        }

        val point = Point(currentLat, currentLng)
        mapView?.map?.move(com.yandex.mapkit.map.CameraPosition(point, 17.0f, 0.0f, 0.0f))
    }

    override fun onStart() {
        super.onStart()
        if (mapKitInited) {
            MapKitFactory.getInstance().onStart()
        }
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (mapKitInited) {
            MapKitFactory.getInstance().onStop()
        }
        mapView?.onStop()
    }

    override fun onDestroyView() {
        mapView = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_PICK_LOCATION = "pickLocation"

        fun newInstance(pickLocation: Boolean = false): MapFragment {
            val fragment = MapFragment()
            fragment.arguments = bundleOf(ARG_PICK_LOCATION to pickLocation)
            return fragment
        }
    }
}
