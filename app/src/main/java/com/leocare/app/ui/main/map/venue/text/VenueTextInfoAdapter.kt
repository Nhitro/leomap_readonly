package com.leocare.app.ui.main.map.venue.text

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.leocare.app.R

class VenueTextInfoAdapter :
        ListAdapter<VenueTextInfo, VenueTextInfoViewHolder>(VenueTextInfoDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueTextInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_text_venue_line, parent, false)
        return VenueTextInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VenueTextInfoViewHolder, position: Int) {
        holder.updateViewHolder(getItem(position))
    }

}