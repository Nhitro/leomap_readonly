package com.leocare.app.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.leocare.app.R
import com.leocare.app.permission.LocationManager
import com.leocare.app.ui.main.map.MapFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeomapActivity : AppCompatActivity(), LocationManager {

    companion object {
        const val ACCESS_TOKEN_BUNDLE_KEY = "com.leocare.app.ui.leomapactivity.token"
        const val LOCATION_TIME_LAPS_MINIMUM_DURATION = 15 * 60 * 1000L
    }

    private val mLocationPermissionLauncher = createRequestLocationPermissionLauncher()

    private val mViewModel: LeomapViewModel by viewModels()

    private val mLocationCallback = createLocationCallback()

    private val mEnableGpsRequestLauncher = createEnableGpsRequestLauncher()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var mLocationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leomap)

        // We have to give user data such as token to Map Fragment
        giveUserDataToHomeFragment()

        // New api objects for location update
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = LOCATION_TIME_LAPS_MINIMUM_DURATION
    }

    override fun onStart() {
        super.onStart()
        listenUserLocation()
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun isLocationPermissionGranted(): Boolean {
        val result =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return PackageManager.PERMISSION_GRANTED == result
    }

    override fun requestLocationToUser() {
        // The first step is to check if GPS is enabled
        val checkLocationSettings = LocationServices.getSettingsClient(this).checkLocationSettings(
                LocationSettingsRequest
                        .Builder()
                        .addLocationRequest(mLocationRequest)
                        .build()
        )

        checkLocationSettings.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                // GPS is okay, let's request the location permission
                explainAndRequestLocationPermissionToUser()
            } catch (exception: ApiException) {
                // Could we ask enable gps to user ?
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvableApiException = exception as ResolvableApiException?
                    resolvableApiException?.apply {
                        // Show the dialog requesting GPS
                        mEnableGpsRequestLauncher.launch(
                                IntentSenderRequest.Builder(resolution.intentSender).build()
                        )
                    }
                }
            }
        }
    }

    /**
     * This method is in charge of giving extras info to home fragment through [MapFragmentArgs]
     * such as user token
     */
    private fun giveUserDataToHomeFragment() {
        // We have to give token to map fragment
        var userToken: String? = null

        intent.extras?.apply {
            userToken = getString(ACCESS_TOKEN_BUNDLE_KEY)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.leomap_host_fragment) as NavHostFragment

        navHostFragment.navController.setGraph(
                R.navigation.map_graph,
                MapFragmentArgs(userToken).toBundle()
        )
    }

    /**
     * This method is in charge of creating the [ActivityResultLauncher] that will be used for
     * the enable gps request. A system dialog is shown explaining to user that he should enable
     * gps for a better user experience. If user agrees, the next step is request location permission
     */
    private fun createEnableGpsRequestLauncher(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                explainAndRequestLocationPermissionToUser()
            }
        }
    }

    /**
     * This method is in charge of checking if user needs rationale permission request or a simple
     * one
     */
    private fun explainAndRequestLocationPermissionToUser() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.rationale_permission_dialog_title)
                    .setMessage(R.string.rationale_permission_dialog_message)
                    .setIcon(R.drawable.ic_twotone_not_listed_location_24)
                    .setPositiveButton(android.R.string.ok) { _, _ -> requestLocationPermission() }
                    .setNegativeButton(R.string.rationale_permission_dialog_negative_button_label) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() =
            mLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * This method is in charge of creating the [ActivityResultLauncher] that will be used for
     * the location request. A dialog is shown with the aim to explain what happened according
     * to the permission result.
     */
    private fun createRequestLocationPermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // We can now listen user location
                listenUserLocation()
                // Notice the user that the location is enabled
                showLocationResultDialog(
                        R.string.location_granted_dialog_title,
                        R.string.location_granted_dialog_message,
                        R.drawable.ic_twotone_location_on_24,
                        R.string.location_granted_dialog_button_label
                )
            } else {
                // Remind gently the user that the location can still be enabled later
                showLocationResultDialog(
                        R.string.location_denied_dialog_title,
                        R.string.location_denied_dialog_message,
                        R.drawable.ic_twotone_location_off_24,
                        R.string.location_denied_dialog_button_label
                )
            }
        }
    }

    /**
     * This method is in charge of requesting location updates
     */
    @SuppressLint("MissingPermission")
    private fun listenUserLocation() {
        if (isLocationPermissionGranted()) {
            LocationCallback()
            mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
            )
        }
    }

    /**
     * This method is in charge of creating LocationCallback that will be notified of user location
     * change. Each time callback is called, we update the activity view model in order to keep
     * up to date the activity state and to notify observers
     */
    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mViewModel.updateUserLocation(locationResult)
            }
        }
    }

    /**
     * Shows a simple alert dialog that dismisses on positive button click
     */
    private fun showLocationResultDialog(
            titleId: Int,
            messageId: Int,
            iconId: Int,
            positiveMessageId: Int
    ) {
        AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setIcon(iconId)
                .setPositiveButton(positiveMessageId) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }
}