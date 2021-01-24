package com.leocare.app.ui.main.map.venue.photo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.leocare.app.R
import com.leocare.data.model.VenuePhotoModel

class VenuePhotoAdapter : ListAdapter<VenuePhotoModel, VenuePhotoViewHolder>(VenuePhotoDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenuePhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.holder_photo_venue, parent, false)
        return VenuePhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VenuePhotoViewHolder, position: Int) {
        holder.updateViewHolder(getItem(position))
    }

}