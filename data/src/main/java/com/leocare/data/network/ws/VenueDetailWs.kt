package com.leocare.data.network.ws

import com.leocare.data.network.raw.Venue
import io.reactivex.rxjava3.core.Single

interface VenueDetailWs {

    fun fetchVenueDetail(venueId: String, userToken: String): Single<Venue>

}