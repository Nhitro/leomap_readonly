package com.leocare.data.network.ws.impl

import com.leocare.data.BuildConfig
import com.leocare.data.network.api.VenueLikeApi
import com.leocare.data.network.ws.VenueLikeWs
import io.reactivex.rxjava3.core.Completable

class VenueLikeWsImpl constructor(
        private val venueLikeApi: VenueLikeApi
) : VenueLikeWs {

    override fun likeVenue(like: Boolean, venueId: String, userToken: String): Completable {
        return venueLikeApi
                .likeVenue(venueId, like, userToken, BuildConfig.FOURSQUARE_API_VERSION)
                .ignoreElement()
    }

}