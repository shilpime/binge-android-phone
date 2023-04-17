package com.ttn.ttnplayer.player

import android.content.res.Resources
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.ttn.ttnplayer.R
import java.util.*

class TtnTrackNameProvider
/**
 * @param resources Resources from which to obtain strings.
 */(private val resources: Resources) : TrackNameProvider {
    override fun getTrackName(format: Format): String {
        val trackName: String
        val trackType = inferPrimaryTrackType(format)
        trackName = when (trackType) {
            C.TRACK_TYPE_VIDEO -> { /* trackName =
                    joinWithSeparator(
                            buildRoleString(format), buildResolutionString(format), buildBitrateString(format));*/
                buildWidthResolutionString(format)
            }
            C.TRACK_TYPE_AUDIO -> {
                buildLanguageOrLabelString(format)
                /*joinWithSeparator(
                        buildLanguageOrLabelString(format),
                        buildAudioChannelString(format),
                        buildBitrateString(format))*/
            }
            else -> {
                buildLanguageOrLabelString(format)
            }
        }
        return if (trackName.isEmpty()) "" else trackName
    }

    private fun buildWidthResolutionString(format: Format): String {
        return format.width.toString()
    }

    private fun buildResolutionString(format: Format): String {
        val width = format.width
        val height = format.height
        return if (width == Format.NO_VALUE || height == Format.NO_VALUE) "" else resources.getString(R.string.exo_track_resolution, width, height)
    }

    private fun buildBitrateString(format: Format): String {
        val bitrate = format.bitrate
        return if (bitrate == Format.NO_VALUE) "" else resources.getString(R.string.exo_track_bitrate, bitrate / 1000000f)
    }

    private fun buildAudioChannelString(format: Format): String {
        val channelCount = format.channelCount
        return if (channelCount < 1) {
            ""
        } else when (channelCount) {
            1 -> resources.getString(R.string.exo_track_mono)
            2 -> resources.getString(R.string.exo_track_stereo)
            6, 7 -> resources.getString(R.string.exo_track_surround_5_point_1)
            8 -> resources.getString(R.string.exo_track_surround_7_point_1)
            else -> resources.getString(R.string.exo_track_surround)
        }
    }

    private fun buildLanguageOrLabelString(format: Format): String {
        val languageAndRole = joinWithSeparator(buildLanguageString(format), buildRoleString(format))
        return if (TextUtils.isEmpty(languageAndRole)) buildLabelString(format) else languageAndRole
    }

    private fun buildLabelString(format: Format): String {
        return if (TextUtils.isEmpty(format.label)) "" else format.label!!
    }

    private fun buildLanguageString(format: Format): String {
        val language = format.language
        if (TextUtils.isEmpty(language) || C.LANGUAGE_UNDETERMINED == language) {
            return ""
        }
        val locale = if (Util.SDK_INT >= 21) Locale.forLanguageTag(language) else Locale(language)
        return locale.displayName
    }

    private fun buildRoleString(format: Format): String {
        var roles = ""
        if (format.roleFlags and C.ROLE_FLAG_ALTERNATE != 0) {
            roles = resources.getString(R.string.exo_track_role_alternate)
        }
        if (format.roleFlags and C.ROLE_FLAG_SUPPLEMENTARY != 0) {
            roles = joinWithSeparator(roles, resources.getString(R.string.exo_track_role_supplementary))
        }
        if (format.roleFlags and C.ROLE_FLAG_COMMENTARY != 0) {
            roles = joinWithSeparator(roles, resources.getString(R.string.exo_track_role_commentary))
        }
        if (format.roleFlags and (C.ROLE_FLAG_CAPTION or C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND) != 0) {
            roles = joinWithSeparator(roles, resources.getString(R.string.exo_track_role_closed_captions))
        }
        return roles
    }

    private fun joinWithSeparator(vararg items: String): String {
        var itemList = ""
        for (item in items) {
            if (item.isNotEmpty()) {
                itemList = if (TextUtils.isEmpty(itemList)) {
                    item
                } else {
                    resources.getString(R.string.exo_item_list, itemList, item)
                }
            }
        }
        return itemList
    }

    companion object {
        private fun inferPrimaryTrackType(format: Format): Int {
            val trackType = MimeTypes.getTrackType(format.sampleMimeType)
            if (trackType != C.TRACK_TYPE_UNKNOWN) {
                return trackType
            }
            if (MimeTypes.getVideoMediaMimeType(format.codecs) != null) {
                return C.TRACK_TYPE_VIDEO
            }
            if (MimeTypes.getAudioMediaMimeType(format.codecs) != null) {
                return C.TRACK_TYPE_AUDIO
            }
            if (format.width != Format.NO_VALUE || format.height != Format.NO_VALUE) {
                return C.TRACK_TYPE_VIDEO
            }
            return if (format.channelCount != Format.NO_VALUE || format.sampleRate != Format.NO_VALUE) {
                C.TRACK_TYPE_AUDIO
            } else C.TRACK_TYPE_UNKNOWN
        }
    }

}