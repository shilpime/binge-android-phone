package com.tatasky.binge.utils

import com.tatasky.binge.data.networking.models.response.ConfigResponse
import java.util.HashMap

object ProvidersCache {
    private val mAllowedProvidersList: MutableList<String> = arrayListOf()
    val allowedProviderList: List<String> = mAllowedProvidersList

    fun setAllowedProvidersList(list: List<String>?) {
        list?.let {
            mAllowedProvidersList.clear()
            mAllowedProvidersList.addAll(list)
        }
    }


    private val mAvailableProviders: HashMap<String, ConfigResponse.AvailableProviders> = HashMap<String, ConfigResponse.AvailableProviders>()
    val availableProviders: HashMap<String, ConfigResponse.AvailableProviders> = mAvailableProviders

    fun setAvailableProviders(map : HashMap<String, ConfigResponse.AvailableProviders>?) {
        map?.let {
            mAvailableProviders.clear()
            mAvailableProviders.putAll(map)
        }
    }
}