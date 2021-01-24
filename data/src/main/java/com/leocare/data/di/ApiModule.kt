package com.leocare.data.di

import com.leocare.data.BuildConfig
import com.leocare.data.network.api.VenueApi
import com.leocare.data.network.api.VenueDetailApi
import com.leocare.data.network.api.VenueLikeApi
import com.leocare.data.network.api.VenuePhotoApi
import com.leocare.data.network.ws.VenueDetailWs
import com.leocare.data.network.ws.VenueLikeWs
import com.leocare.data.network.ws.VenuePhotoWs
import com.leocare.data.network.ws.VenueWs
import com.leocare.data.network.ws.impl.VenueDetailWsImpl
import com.leocare.data.network.ws.impl.VenueLikeWsImpl
import com.leocare.data.network.ws.impl.VenuePhotoWsImpl
import com.leocare.data.network.ws.impl.VenueWsImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class ApiModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        // Only used in debug
        val loggingInterceptor = HttpLoggingInterceptor()

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit.Builder()
                    .addConverterFactory(MoshiConverterFactory.create(
                            Moshi.Builder()
                                    .add(KotlinJsonAdapterFactory())
                                    .build()
                    ))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .baseUrl(BuildConfig.FOURSQUARE_API_URL)
                    .client(okHttpClient)
                    .build()

    @Singleton
    @Provides
    fun provideVenueWs(retrofit: Retrofit): VenueWs = VenueWsImpl(retrofit.create(VenueApi::class.java))

    @Singleton
    @Provides
    fun provideVenuePhotoWs(retrofit: Retrofit): VenuePhotoWs = VenuePhotoWsImpl(retrofit.create(VenuePhotoApi::class.java))

    @Singleton
    @Provides
    fun provideVenueDetailWs(retrofit: Retrofit): VenueDetailWs = VenueDetailWsImpl(retrofit.create(VenueDetailApi::class.java))

    @Singleton
    @Provides
    fun provideVenueLikeWs(retrofit: Retrofit): VenueLikeWs = VenueLikeWsImpl(retrofit.create(VenueLikeApi::class.java))

}