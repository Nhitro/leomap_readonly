package com.leocare.data.network.raw

data class Response(
        val venues: List<Venue>?,
        val venue: Venue?,
        val photos: Photos?
)