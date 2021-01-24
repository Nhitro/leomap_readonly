package com.leocare.data.network.api

import com.leocare.data.network.raw.Root
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueDetailApi {

    @GET("{VENUE_ID}")
    fun fetchVenueDetail(
            @Path(value = "VENUE_ID") venueId: String,
            @Query("oauth_token") userToken: String,
            @Query("v") foursquareApiVersion: String
    ): Single<Root>

}