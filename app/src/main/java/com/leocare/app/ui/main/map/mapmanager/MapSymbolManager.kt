package com.leocare.app.ui.main.map.mapmanager

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property

/**
 * Class in charge of creating and deleting [Symbol]
 */
class MapSymbolManager {

    private lateinit var mSymbolManager: SymbolManager

    /**
     * This method is in charge of creating [SymbolManager]
     */
    fun initialize(mapview: MapView, mapboxMap: MapboxMap, style: Style) {
        mSymbolManager = SymbolManager(mapview, mapboxMap, style)
    }

    /**
     * This method is in charge of creating a [Symbol] according the [latitude] and [longitude]
     * @param latitude  is the latitude of the future marker
     * @param longitude is the longitude of the future marker
     * @param name      is the text to show on the future marker
     */
    fun createSymbol(latitude: Double, longitude: Double, name: String): Symbol {
        return mSymbolManager.create(
                SymbolOptions()
                        .withIconSize(2f)
                        .withIconImage(MapImageManager.MAP_MARK_ICON)
                        .withIconAnchor(Property.ICON_ANCHOR_TOP)
                        .withLatLng(LatLng(latitude, longitude))
                        .withTextField(name)
                        .withTextAnchor(Property.TEXT_ANCHOR_BOTTOM)
        )
    }

    /**
     * This method is in charge of creating a favorite [Symbol] according the [latitude] and [longitude]
     * @param latitude  is the latitude of the future symbol
     * @param longitude is the longitude of the future symbol
     * @param name      is the text to show on the future symbol
     */
    fun createFavoriteSymbol(latitude: Double, longitude: Double, name: String): Symbol {
        return mSymbolManager.create(
                SymbolOptions()
                        .withIconSize(2f)
                        .withIconImage(MapImageManager.MAP_FAVOURITE_ICON)
                        .withIconAnchor(Property.ICON_ANCHOR_TOP)
                        .withLatLng(LatLng(latitude, longitude))
                        .withTextField(name)
                        .withTextAnchor(Property.TEXT_ANCHOR_BOTTOM)
        )
    }

    fun onDestroy() = mSymbolManager.onDestroy()

    fun setOnClickListener(onSymbolClickListener: OnSymbolClickListener) = mSymbolManager.addClickListener(onSymbolClickListener)

    fun updateSymbol(symbol: Symbol) = mSymbolManager.update(symbol)
}