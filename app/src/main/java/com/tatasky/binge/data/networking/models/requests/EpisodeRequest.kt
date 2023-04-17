package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class EpisodeRequest(val episodeId: String,
                          val profileId: String?,
                          val subscriberId: String?)