package com.leocare.data.network.ws

import io.reactivex.rxjava3.core.Completable

interface VenueLikeWs {

    fun likeVenue(like: Boolean, venueId: String, userToken: String): Completable

}