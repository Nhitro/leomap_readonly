package com.leocare.app.ui.main.map.venue.text

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leocare.app.R
import com.leocare.app.databinding.HolderTextVenueLineBinding

/**
 * Shows a simple cell with a icon and a text view
 */
class VenueTextInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mBinding = HolderTextVenueLineBinding.bind(itemView)

    fun updateViewHolder(venueTextInfo: VenueTextInfo) {
        mBinding.venueText.text = venueTextInfo.text

        Glide.with(mBinding.root)
                .load(convertVenueTextInfoTypeIntoIcon(venueTextInfo.venueTextInfoType))
                .into(mBinding.venueIcon)
    }

    /**
     * This method is in charge of converting a [VenueTextInfoType] into a drawable id
     */
    private fun convertVenueTextInfoTypeIntoIcon(venueTextInfoType: VenueTextInfoType): Int {
        return when (venueTextInfoType) {
            VenueTextInfoType.ADDRESS -> R.drawable.ic_twotone_room_24
            VenueTextInfoType.PHONE_NUMBER -> R.drawable.ic_twotone_local_phone_24
            VenueTextInfoType.WEBSITE_URL -> R.drawable.ic_twotone_link_24
            VenueTextInfoType.IS_OPEN -> R.drawable.ic_twotone_today_24
            VenueTextInfoType.HOURS -> R.drawable.ic_twotone_schedule_24
            VenueTextInfoType.MAIL -> R.drawable.ic_twotone_email_24
            VenueTextInfoType.BEEN_HERE -> R.drawable.ic_twotone_beenhere_24
        }
    }
}