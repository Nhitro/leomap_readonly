package com.leocare.data.network.ws

import com.leocare.data.network.raw.Venue
import io.reactivex.rxjava3.core.Single

interface VenueWs {

    fun fetchVenues(userToken: String, latitude: Double, longitude: Double, radius: Int): Single<List<Venue>>

}