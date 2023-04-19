package com.tatasky.binge.interfaces

import com.tatasky.binge.data.networking.models.response.DeviceList

interface DeviceDeleteClickListener {
    fun onDeviceDelete(
        item: DeviceList
    )
}