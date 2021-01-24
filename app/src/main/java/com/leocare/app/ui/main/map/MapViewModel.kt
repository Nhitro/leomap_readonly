package com.leocare.app.ui.main.map

import android.location.Location
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.leocare.data.model.VenueModel
import com.leocare.data.network.raw.Photos
import com.leocare.data.network.raw.Venue
import com.leocare.data.network.ws.VenueLikeWs
import com.leocare.data.network.ws.VenueWs
import com.leocare.data.usecase.FetchVenueRelatedDataUseCase
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapViewModel @ViewModelInject constructor(
        private val mVenueWs: VenueWs,
        private val mVenueLikeWs: VenueLikeWs,
        private val mFetchVenueRelatedDataUseCase: FetchVenueRelatedDataUseCase
) : ViewModel() {

    companion object {
        private val TAG = MapViewModel::class.java.name
    }

    private val mCompositeDisposable = CompositeDisposable()

    private val mMutableVenuesLiveData = MutableLiveData<List<Venue>>()

    private val mMutableSelectedVenueLiveData = MutableLiveData<VenueModel>()

    private lateinit var mUserToken: String

    val venuesLiveData: LiveData<List<Venue>>
        get() = mMutableVenuesLiveData

    val selectedVenueLiveData: LiveData<VenueModel>
        get() = mMutableSelectedVenueLiveData

    fun initViewModel(userToken: String) {
        mUserToken = userToken
    }

    /**
     * Fetch venues around the location thanks to [location.latitude] and [location.longitude]
     */
    fun fetchVenues(location: Location) {
        fetchVenues(location.latitude, location.longitude)
    }

    /**
     * Fetch venues around the couple lat long
     */
    fun fetchVenues(latitude: Double, longitude: Double) {
        val currentVenues = mMutableVenuesLiveData.value ?: ArrayList()

        mCompositeDisposable.add(
                mVenueWs.fetchVenues(mUserToken, latitude, longitude, 500)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                { mMutableVenuesLiveData.postValue(it + currentVenues) },
                                { Log.e(TAG, "Une erreur est survenue lors de la récupération des lieux", it) }
                        )
        )
    }

    /**
     * Like venue in Foursquare
     */
    fun likeVenue(like: Boolean, venueId: String) {
        mCompositeDisposable.add(
                mVenueLikeWs.likeVenue(like, venueId, mUserToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe {
                            mMutableSelectedVenueLiveData.value?.apply {
                                mMutableSelectedVenueLiveData.postValue(
                                        VenueModel(
                                                id, name, description, address, city, phoneNumber, url,
                                                like, lat, lng, categoryName, hoursModel, venuePhotoModelList
                                        )
                                )
                            }
                        }
        )
    }

    /**
     * This method is in charge of finding the venue that the user selected according [latitude]
     * and [longitude]. Once it is done, [fetchVenueRelatedData] is called
     */
    fun userSelectedSymbolAt(latitude: Double, longitude: Double) {
        mMutableVenuesLiveData.value?.apply {
            stream()
                    .filter { venue -> venue.location.lat == latitude && venue.location.lng == longitude }
                    .findFirst()
                    .ifPresent {
                        fetchVenueRelatedData(it)
                    }
        }
    }

    /**
     * Fetch related data to venue such as [Photos], [Location], [Venue] in order to show it to user
     * @see selectedVenueLiveData
     */
    private fun fetchVenueRelatedData(venue: Venue) {
        mCompositeDisposable.add(
                mFetchVenueRelatedDataUseCase.execute(mUserToken, venue)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                { mMutableSelectedVenueLiveData.postValue(it) },
                                { Log.e(TAG, "Une erreur est survenue lors de la récupération du lieu sélectionné", it) }
                        )
        )
    }

}