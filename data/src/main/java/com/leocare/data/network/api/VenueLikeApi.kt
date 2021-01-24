package com.leocare.data.network.api

import com.leocare.data.network.raw.Root
import io.reactivex.rxjava3.core.Single
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueLikeApi {

    @POST("{VENUE_ID}/like")
    fun likeVenue(
            @Path(value = "VENUE_ID") venueId: String,
            @Query("set") like: Boolean,
            @Query("oauth_token") userToken: String,
            @Query("v") foursquareApiVersion: String
    ): Single<Root>

}