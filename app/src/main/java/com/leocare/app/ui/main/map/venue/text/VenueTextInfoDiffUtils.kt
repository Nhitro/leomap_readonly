package com.leocare.app.ui.main.map.venue.text

import androidx.recyclerview.widget.DiffUtil

/**
 * Used in [VenueTextInfoAdapter]
 */
class VenueTextInfoDiffUtils : DiffUtil.ItemCallback<VenueTextInfo>() {

    override fun areItemsTheSame(oldItem: VenueTextInfo, newItem: VenueTextInfo): Boolean {
        return oldItem.venueTextInfoType == newItem.venueTextInfoType
    }

    override fun areContentsTheSame(oldItem: VenueTextInfo, newItem: VenueTextInfo): Boolean {
        return oldItem.text == newItem.text
    }

}