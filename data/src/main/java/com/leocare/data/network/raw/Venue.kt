package com.leocare.data.network.raw

data class Venue(
        val id: String,
        val name: String,
        val description: String?,
        val location: Location,
        val categories: List<Category>,
        val url: String?,
        val contact: Contact?,
        val beenHere: BeenHere?,
        val hours: Hours?,
        val venuePage: VenuePage?,
        val like: Boolean?
)