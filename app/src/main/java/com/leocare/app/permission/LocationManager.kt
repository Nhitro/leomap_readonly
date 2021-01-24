package com.leocare.app.permission

/**
 * The [LocationManager] is in charge of managing everything about the location and permissions
 */
interface LocationManager {

    fun isLocationPermissionGranted(): Boolean

    /**
     * Requests location permission to the user.
     * Check all requirements (is GPS on? Permission ?) before listening user location updates
     * Process is different depending on we should or not explain why before requesting permission
     */
    fun requestLocationToUser()

}