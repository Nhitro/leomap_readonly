package com.leocare.data.network.ws.impl

import com.leocare.data.BuildConfig
import com.leocare.data.network.api.VenueApi
import com.leocare.data.network.raw.Venue
import com.leocare.data.network.ws.VenueWs
import io.reactivex.rxjava3.core.Single

class VenueWsImpl constructor(
        private val venueApi: VenueApi
) : VenueWs {

    companion object {
        const val MAX_RADIUS = "100"
    }

    override fun fetchVenues(
            userToken: String,
            latitude: Double,
            longitude: Double,
            radius: Int
    ): Single<List<Venue>> {
        return venueApi
                .fetchVenues(
                        userToken,
                        "${latitude.toFloat()},${longitude.toFloat()}",
                        radius.toString(),
                        MAX_RADIUS,
                        BuildConfig.FOURSQUARE_API_VERSION
                )
                .map { root -> root.response.venues }
    }

}