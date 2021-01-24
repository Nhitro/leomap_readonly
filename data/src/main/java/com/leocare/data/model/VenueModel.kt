package com.leocare.data.model

data class VenueModel(
        val id: String,
        val name: String,
        val description: String,
        val address: String,
        val city: String,
        val phoneNumber: String,
        val url: String,
        val like: Boolean,
        val lat: Double,
        val lng: Double,
        val categoryName: String,
        val hoursModel: HoursModel?,
        val venuePhotoModelList: List<VenuePhotoModel>
)