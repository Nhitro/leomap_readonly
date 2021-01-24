package com.leocare.app.ui.main.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.leocare.app.R
import com.leocare.app.databinding.FragmentMapBinding
import com.leocare.app.extension.convertDpToPixel
import com.leocare.app.extension.observeOnce
import com.leocare.app.permission.LocationManager
import com.leocare.app.ui.main.LeomapViewModel
import com.leocare.app.ui.main.map.mapmanager.MapImageManager
import com.leocare.app.ui.main.map.mapmanager.MapSymbolManager
import com.leocare.app.ui.main.map.venue.photo.VenuePhotoAdapter
import com.leocare.app.ui.main.map.venue.text.VenueTextInfo
import com.leocare.app.ui.main.map.venue.text.VenueTextInfoAdapter
import com.leocare.app.ui.main.map.venue.text.VenueTextInfoType
import com.leocare.data.model.VenueModel
import com.leocare.data.network.raw.Venue
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.UiSettings
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.pluginscalebar.ScaleBarOptions
import com.mapbox.pluginscalebar.ScaleBarPlugin
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment is in charge of showing the map view and related views
 */
@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {

    private val mBinding: FragmentMapBinding by viewBinding()

    private val mLocationViewModel: LeomapViewModel by activityViewModels()

    private val mViewModel: MapViewModel by viewModels()

    private val mArgs: MapFragmentArgs by navArgs()

    private val mMapSymbolManager = MapSymbolManager()

    private val mMapImageManager = MapImageManager()

    private val mCircleMarkerList: MutableList<Symbol> = ArrayList()

    private var mLocationComponent: LocationComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize map
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))

        // initialize view model
        mViewModel.initViewModel(mArgs.userToken ?: "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.mapView.onCreate(savedInstanceState)

        // Initialize the map and its style
        initializeMapAccordingLocationManager()

        // Setup listeners or behaviors tied to location
        (activity as? LocationManager)?.apply {
            listenNextLocationForMapSetting(this)
            manageLocationFabAccordingLocationManager(this)
        }

        // observe live data
        mViewModel.venuesLiveData.observe(viewLifecycleOwner, observeVenues())
        mViewModel.selectedVenueLiveData.observe(viewLifecycleOwner, showBottomSheetAccordingSelectedVenue())
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mBinding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCircleMarkerList.clear()
        mMapSymbolManager.onDestroy()
        mBinding.mapView.onDestroy()
    }

    /**
     * Update map symbols according to venues
     */
    private fun observeVenues(): Observer<List<Venue>> {
        return Observer { venueList ->
            venueList.forEach { venue ->
                createOrReplaceSymbol(
                        venue.location.lat,
                        venue.location.lng,
                        venue.name,
                        venue.like ?: false,
                        false
                )
            }
        }
    }

    /**
     * Synchronizes the bottom sheet according the last venue selected by the user
     */
    private fun showBottomSheetAccordingSelectedVenue(): Observer<VenueModel> {
        val venuePhotoAdapter = VenuePhotoAdapter()
        val venueTextInfoAdapter = VenueTextInfoAdapter()
        val bottomSheet = mBinding.mapVenueDetailContainer
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isFitToContents = false

        mBinding.mapVenueDetailSheet.venueTextInformationList.adapter = venueTextInfoAdapter
        mBinding.mapVenueDetailSheet.venuePhotos.adapter = venuePhotoAdapter

        return Observer { venueModel ->
            // Update the bottom sheet view according the new venue model
            mBinding.mapVenueDetailSheet.apply {
                venueName.text = venueModel.name
                venueType.text = venueModel.categoryName
                venueDescription.text =
                        if (venueModel.description.isEmpty()) getString(R.string.venue_detail_no_description)
                        else venueModel.description

                // Show the bottom sheet by changing its state
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
            updateOnClickListeners(venueModel)

            // Update recycler views data
            venueTextInfoAdapter.submitList(createVenueTextInfoFrom(venueModel))
            venuePhotoAdapter.submitList(venueModel.venuePhotoModelList)
        }
    }

    /**
     * Update button click listeners with the new selected [VenueModel]
     */
    private fun updateOnClickListeners(venueModel: VenueModel) {
        // Click on call button open the call application
        mBinding.mapVenueDetailSheet.venueCall.setOnClickListener {
            startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${venueModel.phoneNumber}"))
            )
        }

        // Check if the url is valid
        var url = venueModel.url
        if (url.isBlank()) {
            // Otherwise, try to redirect the user on google !
            url = getString(R.string.google_search_link, "${venueModel.name} ${venueModel.city}")
        }
        // Click on website button open the browser
        mBinding.mapVenueDetailSheet.venueWebsite.setOnClickListener {
            startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        }

        // Manage favorite button
        val favoriteButton = mBinding.mapVenueDetailSheet.venueFavorites
        // Change tint color according like or not
        val tintColor = if (venueModel.like) R.color.primary_color else R.color.color_not_liked
        // Update the button tint
        changeDrawableTopTintColorOf(favoriteButton, tintColor)
        // And the symbol if the is venue is a favorite one or note
        createOrReplaceSymbol(venueModel.lat, venueModel.lng, venueModel.name, isFavorite = venueModel.like, mustReplaceSymbol = true)
        // Finally, update on click listener of favorite button
        favoriteButton.setOnClickListener {
            mViewModel.likeVenue(!venueModel.like, venueModel.id)
        }
    }

    /**
     * Builds a list of [VenueTextInfo] from a [VenueModel]
     * @see VenueTextInfoType
     */
    private fun createVenueTextInfoFrom(venueModel: VenueModel): List<VenueTextInfo> {
        val venueTextInfoList = ArrayList<VenueTextInfo>()

        venueTextInfoList.add(VenueTextInfo(VenueTextInfoType.ADDRESS, "${venueModel.address}, ${venueModel.city}"))
        venueTextInfoList.add(VenueTextInfo(VenueTextInfoType.WEBSITE_URL, venueModel.url))
        venueTextInfoList.add(VenueTextInfo(VenueTextInfoType.PHONE_NUMBER, venueModel.phoneNumber))

        venueModel.hoursModel?.apply {
            venueTextInfoList.add(VenueTextInfo(VenueTextInfoType.IS_OPEN, lastStatus))
            venueTextInfoList.add(VenueTextInfo(VenueTextInfoType.HOURS, renderedTimes.joinToString("\n")))
        }

        return venueTextInfoList
    }

    /**
     * This method is in charge of initializing the map and its components such as [MapImageManager]
     * and [MapSymbolManager]. It is also here that we listen events such as camera idle or symbol
     * click.
     */
    private fun initializeMapAccordingLocationManager() {
        mBinding.mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) {
                val uiSettings: UiSettings = mapboxMap.uiSettings
                uiSettings.isCompassEnabled = false

                // Initialize images
                mMapImageManager.addImagesToStyle(requireContext(), it)

                // Initialize map symbol manager
                mMapSymbolManager.initialize(mBinding.mapView, mapboxMap, it)
                mMapSymbolManager.setOnClickListener(
                        OnSymbolClickListener { symbol ->
                            mViewModel.userSelectedSymbolAt(symbol.latLng.latitude, symbol.latLng.longitude)
                            true
                        }
                )

                // Listen camera moves and request venues when the camera idles !
                mapboxMap.addOnCameraIdleListener {
                    val target = mapboxMap.cameraPosition.target
                    mViewModel.fetchVenues(target.latitude, target.longitude)
                }

                mapboxMap.addOnCameraMoveListener {
                    val bottomSheetBehavior = BottomSheetBehavior.from(mBinding.mapVenueDetailContainer)
                    // We cannot move on map if the bottom sheet is full expanded
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                val scaleBarPlugin = ScaleBarPlugin(mBinding.mapView, mapboxMap)
                scaleBarPlugin.create(
                        ScaleBarOptions(requireContext())
                                .setMarginTop(convertDpToPixel(36f))
                                .setMarginLeft(convertDpToPixel(16f))
                )
            }
        }
    }

    /**
     * This method is in charge of listening the first location of the user in order to setup
     * the map according to this one
     */
    private fun listenNextLocationForMapSetting(locationManager: LocationManager) {
        mLocationViewModel.userLocationLiveData.observeOnce(viewLifecycleOwner) {
            getMapboxStyleThenRun { mapboxMap, style ->
                if (locationManager.isLocationPermissionGranted()) {
                    showUserPositionOnMap(mapboxMap, style)
                    updateMapPosition(it.lastLocation.latitude, it.lastLocation.longitude)
                    mViewModel.fetchVenues(it.lastLocation)
                }
            }
        }
    }

    /**
     * This method is in charge of the behavior when user clicks on the floating action button
     * Behavior is to simply locate user on the map. But before we can do it, we have to check if we
     * are granted to do so. We ask to [LocationManager] if we can, if not, we ask the permission to
     * the user, otherwise we update the map.
     */
    private fun manageLocationFabAccordingLocationManager(locationManager: LocationManager) {
        mBinding.mapRequestLocation.setOnClickListener {
            // If location is not granted, we must ask it
            if (!locationManager.isLocationPermissionGranted()) {
                locationManager.requestLocationToUser()
            } else {
                mLocationViewModel.userLocationLiveData.value?.apply {
                    // otherwise, zoom on its position
                    updateMapPosition(this.lastLocation.latitude, this.lastLocation.longitude)
                }
            }
        }
    }

    /**
     * This method is in charge of creating or replacing a symbol according the [latitude] and the
     * [longitude]. The symbol is different according to [isFavorite]
     * @param latitude          is the latitude of the future symbol
     * @param longitude         is the longitude of the future symbol
     * @param name              is the name of the future symbol
     * @param isFavorite        is a flag that identify a liked venue
     * @param mustReplaceSymbol is a flag that allow replacing or note existing symbols
     */
    private fun createOrReplaceSymbol(latitude: Double,
                                      longitude: Double,
                                      name: String,
                                      isFavorite: Boolean,
                                      mustReplaceSymbol: Boolean) {

        val venueLatLng = LatLng(latitude, longitude)

        // Check if symbol is already existing for this venue before adding another one
        val existingSymbol =
                mCircleMarkerList
                        .stream()
                        .filter { symbol -> symbol.latLng.latitude == venueLatLng.latitude }
                        .filter { symbol -> symbol.latLng.longitude == venueLatLng.longitude }
                        .findAny()

        // If the symbol does not exist, we have to create it
        if (!existingSymbol.isPresent) {
            // Is the venue liked ?
            val symbol =
                    if (isFavorite) mMapSymbolManager.createFavoriteSymbol(latitude, longitude, name)
                    else mMapSymbolManager.createSymbol(latitude, longitude, name)

            // Add it to symbol list
            mCircleMarkerList.add(symbol)
        } else if (mustReplaceSymbol) {
            // Change existing one if exists
            existingSymbol.ifPresent {
                it.iconImage =
                        if (isFavorite) MapImageManager.MAP_FAVOURITE_ICON
                        else MapImageManager.MAP_MARK_ICON

                mMapSymbolManager.updateSymbol(it)
            }
        }
    }

    /**
     * This method is in charge of showing user position on map and updating map camera position
     * once application is noticed of its location
     */
    @SuppressLint("MissingPermission")
    private fun showUserPositionOnMap(mapboxMap: MapboxMap, style: Style) {
        val locationComponentOptions =
                LocationComponentOptions.builder(requireContext())
                        .pulseEnabled(true)
                        .pulseColor(Color.BLUE)
                        .pulseAlpha(.4f)
                        .pulseInterpolator(BounceInterpolator())
                        .build()

        val locationComponentActivationOptions =
                LocationComponentActivationOptions
                        .builder(requireContext(), style)
                        .locationComponentOptions(locationComponentOptions)
                        .build()

        val locationComponent = mapboxMap.locationComponent
        locationComponent.activateLocationComponent(locationComponentActivationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.renderMode = RenderMode.COMPASS
        mLocationComponent = locationComponent
    }

    /**
     * This method is in charge of updating the map camera position according a latitude and longitude
     * @param latitude  is the latitude of the user
     * @param longitude is the longitude of the user
     */
    private fun updateMapPosition(latitude: Double, longitude: Double) {
        val position = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15.0)
                .tilt(0.0)
                .build()

        getMapboxMapThenRun {
            it.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
        }
    }

    /**
     * Utility method, avoid boiler plate
     */
    private fun getMapboxStyleThenRun(method: (mapboxMap: MapboxMap, style: Style) -> Unit) {
        mBinding.mapView.getMapAsync { mapboxMap ->
            mapboxMap.getStyle {
                method.invoke(mapboxMap, it)
            }
        }
    }

    /**
     * Utility method, avoid boiler plate
     */
    private fun getMapboxMapThenRun(method: (mapboxMap: MapboxMap) -> Unit) {
        mBinding.mapView.getMapAsync {
            method.invoke(it)
        }
    }

    /**
     * Utility method that changes icon tint of a button
     */
    private fun changeDrawableTopTintColorOf(button: Button, colorTintId: Int) {
        val background: Drawable = button.compoundDrawables[1]
        background.setTint(requireContext().getColor(colorTintId))
        button.setTextColor(requireContext().getColor(colorTintId))
        button.setCompoundDrawables(null, background, null, null)
    }


}