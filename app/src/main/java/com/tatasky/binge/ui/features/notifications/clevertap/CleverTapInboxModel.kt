package com.tatasky.binge.ui.features.notifications.clevertap


data class LinksModel(val kv: KVModel? = null, val url: URLModel? = null)

data class KVModel(val screenData: String?)

data class URLModel(val android: AndroidModel? = null)

data class AndroidModel(val text: String?= null)
