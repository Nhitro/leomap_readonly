package com.leocare.data.network.api

import com.leocare.data.network.raw.Root
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VenuePhotoApi {

    @GET("{VENUE_ID}/photos")
    fun fetchVenuePhotos(
            @Path(value = "VENUE_ID") venueId: String,
            @Query("oauth_token") userToken: String,
            @Query("limit") limit: Int,
            @Query("v") foursquareApiVersion: String
    ): Single<Root>

}