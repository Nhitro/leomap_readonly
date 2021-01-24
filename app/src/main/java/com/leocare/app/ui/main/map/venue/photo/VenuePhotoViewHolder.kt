package com.leocare.app.ui.main.map.venue.photo

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.leocare.app.R
import com.leocare.app.databinding.HolderPhotoVenueBinding
import com.leocare.data.model.VenuePhotoModel

/**
 * Shows a simple photo cell with a image view that loads its image from a url
 */
class VenuePhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mBinding = HolderPhotoVenueBinding.bind(itemView)

    fun updateViewHolder(venuePhotoModel: VenuePhotoModel) {
        val options: RequestOptions = RequestOptions()
                .centerCrop()
                .error(R.drawable.ic_twotone_broken_image_24)

        Glide.with(mBinding.root)
                .load(venuePhotoModel.url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(mBinding.venuePhoto)
    }

}