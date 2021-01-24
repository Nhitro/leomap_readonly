package com.leocare.app.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationResult

class LeomapViewModel @ViewModelInject constructor() : ViewModel() {

    private val mUserLocationMutableLiveData = MutableLiveData<LocationResult>()

    val userLocationLiveData: LiveData<LocationResult>
        get() = mUserLocationMutableLiveData

    fun updateUserLocation(location: LocationResult) {
        mUserLocationMutableLiveData.postValue(location)
    }

}