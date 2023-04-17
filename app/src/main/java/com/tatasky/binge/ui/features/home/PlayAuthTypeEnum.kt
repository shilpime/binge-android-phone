package com.tatasky.binge.ui.features.home

enum class PlayAuthTypeEnum(val value: String) {
        JWT_TOKEN("jwttoken"),
        NONE("none"),
        DRM_TOKENAPI("drm_tokenapi"),
        UNKNOWN("unknown")
}