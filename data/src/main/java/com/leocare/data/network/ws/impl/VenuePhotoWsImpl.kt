package com.leocare.data.network.ws.impl

import com.leocare.data.BuildConfig
import com.leocare.data.network.api.VenuePhotoApi
import com.leocare.data.network.raw.Photos
import com.leocare.data.network.ws.VenuePhotoWs
import io.reactivex.rxjava3.core.Single

class VenuePhotoWsImpl constructor(
        private val venuePhotoApi: VenuePhotoApi
) : VenuePhotoWs {

    companion object {
        const val LIMIT: Int = 30
    }

    override fun fetchVenuePhotos(venueId: String, userToken: String): Single<Photos> {
        return venuePhotoApi
                .fetchVenuePhotos(venueId, userToken, LIMIT, BuildConfig.FOURSQUARE_API_VERSION)
                .map { it.response.photos }
    }

}