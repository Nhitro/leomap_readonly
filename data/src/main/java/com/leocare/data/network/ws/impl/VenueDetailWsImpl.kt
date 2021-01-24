package com.leocare.data.network.ws.impl

import com.leocare.data.BuildConfig
import com.leocare.data.network.api.VenueDetailApi
import com.leocare.data.network.raw.Venue
import com.leocare.data.network.ws.VenueDetailWs
import io.reactivex.rxjava3.core.Single

class VenueDetailWsImpl constructor(
        private val venueDetailApi: VenueDetailApi
) : VenueDetailWs {

    override fun fetchVenueDetail(venueId: String, userToken: String): Single<Venue> {
        return venueDetailApi
                .fetchVenueDetail(venueId, userToken, BuildConfig.FOURSQUARE_API_VERSION)
                .map { it.response.venue }
    }

}