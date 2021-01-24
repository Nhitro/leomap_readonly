package com.leocare.data.network.api

import com.leocare.data.network.raw.Root
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface VenueApi {

    @GET("search?")
    fun fetchVenues(
            @Query("oauth_token") userToken: String,
            @Query("ll") position: String,
            @Query("radius") radius: String,
            @Query("limit") limit: String,
            @Query("v") foursquareApiVersion: String
    ): Single<Root>

}