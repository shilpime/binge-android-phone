package com.tatasky.binge.utils

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.probe.sdk.otherutils.ProbeInterface
import com.tatasky.binge.ui.features.player.PlayerModel
import org.json.JSONObject

fun playerEventRegisterForMitigationSession(sid: String, context : Context) {
    ProbeInterface.registerForMitigationSession(sid, context, true)
}

fun probePlayerEventInitSdk(exoPlayer: SimpleExoPlayer?,
                            playerModel: PlayerModel?,
                            bandWidthMeter: DefaultBandwidthMeter,
                            subscriberId : String) {
    playerModel?.let {
        e("ProbeMitigationSDK", "PlayerModel $playerModel,  bandWidthMeter: $bandWidthMeter")
        val jsonObject = JSONObject()
        jsonObject.put("videoId", playerModel.getContentId() ?: "")
        jsonObject.put("provider", playerModel.getProvider() ?: "")
        jsonObject.put("drm", playerModel.getDrmType() ?: "")
        jsonObject.put("videoUrl", playerModel.getPlaybackUrl() ?: "")
        jsonObject.put("videoTitle", playerModel.getTitle() ?: "")
        jsonObject.put("playerVersion", "2.11.8")
        jsonObject.put("contentType", playerModel.getContentType()?: "")
        jsonObject.put("subscriberId", subscriberId)
        //subscriberId
        e("ProbeMitigationSDK", "jsonObject ${jsonObject.toString()}")
        ProbeInterface.initSdk(exoPlayer, jsonObject.toString())
    }
}


fun probePlayerEventPlayClicked() {
    ProbeInterface.sendEvent("PLAYCLICKED", "")
}

fun probePlayerEventStopped() {
    e("ProbeMitigationSDK","inside probePlayerEventStopped")
    ProbeInterface.sendEvent("STOPPED", "")
}

/*fun probePlayerEventError(errorInfo: String) {
    ProbeInterface.sendEvent("ERROR", errorInfo)
}*/
fun probePlayerEventError(errorCode: String?, errorName: String, errorMsg: String) {
    val jsonvideoAssetDetails = JSONObject()
    jsonvideoAssetDetails.put("ErrorCode", errorCode)
    jsonvideoAssetDetails.put("ErrorName", errorName)
    jsonvideoAssetDetails.put("ErrorDetails", errorMsg)
    e("ProbeMitigationSDK", "ERROR jsonObject ${jsonvideoAssetDetails.toString()}")
    ProbeInterface.sendEvent("ERROR", jsonvideoAssetDetails.toString())
}
fun probPlayerEventsConfigBuffer(loadControlBuilder: DefaultLoadControl.Builder) {
    ProbeInterface.configBufferingProp(loadControlBuilder)
}
fun probPlayerEventConfigEstDownloadRate() {
    ProbeInterface.configEstDownloadRate()
}