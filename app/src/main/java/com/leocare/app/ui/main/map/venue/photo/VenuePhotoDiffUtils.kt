package com.leocare.app.ui.main.map.venue.photo

import androidx.recyclerview.widget.DiffUtil
import com.leocare.data.model.VenuePhotoModel

class VenuePhotoDiffUtils : DiffUtil.ItemCallback<VenuePhotoModel>() {

    override fun areItemsTheSame(oldItem: VenuePhotoModel, newItem: VenuePhotoModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: VenuePhotoModel, newItem: VenuePhotoModel): Boolean {
        return oldItem.url == newItem.url
    }

}