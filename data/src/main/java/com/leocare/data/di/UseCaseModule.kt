package com.leocare.data.di

import com.leocare.data.network.ws.VenueDetailWs
import com.leocare.data.network.ws.VenuePhotoWs
import com.leocare.data.usecase.FetchVenueRelatedDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class UseCaseModule {

    @Singleton
    @Provides
    fun provideFetchVenueRelatedDataUseCase(
            venueDetailWs: VenueDetailWs,
            venuePhotoWs: VenuePhotoWs
    ): FetchVenueRelatedDataUseCase {
        return FetchVenueRelatedDataUseCase(venueDetailWs, venuePhotoWs)
    }

}