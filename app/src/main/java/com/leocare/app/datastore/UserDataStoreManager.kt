package com.leocare.app.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.data
import androidx.datastore.rxjava3.updateDataAsync
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Function
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Manages [DataStore]
 */
class UserDataStoreManager(context: Context) {

    companion object {
        const val ACCESS_TOKEN_PREFERENCE_KEY = "com.leocare.app.datastore.user.token"
    }

    private val mUserDataStore: DataStore<Preferences> =
            RxPreferenceDataStoreBuilder(context, "user").build()

    @ExperimentalCoroutinesApi
    fun readStringData(key: String): @NonNull Single<String> {
        return Single.fromCallable { stringPreferencesKey(key) }
                .flatMap { preferencesKey ->
                    mUserDataStore
                            .data()
                            .firstOrError()
                            .map { preferences -> preferences[preferencesKey] }
                }
    }

    @ExperimentalCoroutinesApi
    fun writeStringData(key: String, value: String): @NonNull Completable {
        return mUserDataStore
                .updateDataAsync(
                        Function { preferences ->
                            Single.fromCallable {
                                val preferencesKey = stringPreferencesKey(key)
                                val mutablePreferences = preferences.toMutablePreferences()

                                // edit preferences
                                mutablePreferences[preferencesKey] = value

                                mutablePreferences.toPreferences()
                            }
                        })
                .ignoreElement()
    }
}