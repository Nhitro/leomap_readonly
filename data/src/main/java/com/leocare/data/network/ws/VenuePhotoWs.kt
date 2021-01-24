package com.leocare.data.network.ws

import com.leocare.data.network.raw.Photos
import io.reactivex.rxjava3.core.Single

interface VenuePhotoWs {

    fun fetchVenuePhotos(venueId: String, userToken: String): Single<Photos>

}