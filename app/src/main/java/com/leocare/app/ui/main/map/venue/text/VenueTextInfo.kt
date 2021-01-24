package com.leocare.app.ui.main.map.venue.text

/**
 * Represents an information about the venue
 * Different kind of information exists, please see [VenueTextInfoType]
 */
data class VenueTextInfo(
        val venueTextInfoType: VenueTextInfoType,
        val text: String
)