package com.tatasky.binge.domain.repositories

import kotlinx.coroutines.flow.Flow

interface DataStorePrefsRepo {

    suspend fun saveGoogleOrFacebookDeferredDeeplinkUriInString(uri: String?)
    fun getGoogleOrFacebookDeferredDeeplinkUriInString(): Flow<String?>
}