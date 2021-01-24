package com.leocare.app.ui.main.map.mapmanager

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.leocare.app.R
import com.mapbox.mapboxsdk.maps.Style

/**
 * Class in charge of managing all images that will be shown in the map
 */
class MapImageManager {

    companion object {
        const val MAP_MARK_ICON = "com.leocare.app.ui.map.map.icon.map_mark_icon"
        const val MAP_FAVOURITE_ICON = "com.leocare.app.ui.map.map.icon.map_favourite_icon"

        private val TAG = MapImageManager::class.java.name
    }

    /**
     * This method is in charge of adding all images to [style]
     * @see Style
     * @see Style.addImage
     */
    fun addImagesToStyle(context: Context, style: Style) {
        loadImageAndAddIt(context, style, R.drawable.map_marker, MAP_MARK_ICON)
        loadImageAndAddIt(context, style, R.drawable.map_favourite, MAP_FAVOURITE_ICON)
    }

    /**
     * Helper method that loads image using [Glide] and add it to [style]
     * @param context       is application context
     * @param style         is mapbox style
     * @param drawableId    is the id of drawable to load
     * @param drawableName  is the name of the drawable
     */
    private fun loadImageAndAddIt(
            context: Context,
            style: Style,
            drawableId: Int,
            drawableName: String
    ) {
        Glide.with(context)
                .load(drawableId)
                .into(
                        object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                                Log.w(TAG, "Map image icon loading cancelled")
                            }

                            override fun onResourceReady(
                                    resource: Drawable,
                                    transition: Transition<in Drawable>?
                            ) {
                                style.addImage(drawableName, resource)
                            }
                        }
                )
    }
}