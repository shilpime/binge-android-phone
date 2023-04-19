package com.tatasky.binge.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tatasky.binge.domain.repositories.DataStorePrefsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class DataStorePrefs(private val ctx: Context) : DataStorePrefsRepo {

    override suspend fun saveGoogleOrFacebookDeferredDeeplinkUriInString(uri: String?) {
        ctx.dataStore.edit { preferences ->
            uri?.let {
                preferences[DATASTORE_PREF_FB_DEFERRED_DEEP_LINK] = uri
            } ?: run {
                preferences.remove(DATASTORE_PREF_FB_DEFERRED_DEEP_LINK)
            }
        }
    }

    override fun getGoogleOrFacebookDeferredDeeplinkUriInString() : Flow<String?> {
        return ctx.dataStore.data.map {
            it[DATASTORE_PREF_FB_DEFERRED_DEEP_LINK]
        }
    }

    companion object {
        private const val DATASTORE_PREF = "binge_datastore_preferences"
        private val DATASTORE_PREF_FB_DEFERRED_DEEP_LINK =
            stringPreferencesKey("DATASTORE_PREF_FB_DEFERRED_DEEP_LINK")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_PREF)
}