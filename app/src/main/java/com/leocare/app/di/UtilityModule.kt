package com.leocare.app.di

import android.app.Application
import com.leocare.app.datastore.UserDataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilityModule {

    @Provides
    @Singleton
    fun provideDatastoreManager(application: Application): UserDataStoreManager {
        return UserDataStoreManager(application.applicationContext)
    }

}