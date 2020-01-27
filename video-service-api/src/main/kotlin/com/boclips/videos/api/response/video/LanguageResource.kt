package com.boclips.videos.api.response.video

import java.util.Locale

data class LanguageResource(val code: String, val displayName: String) {
    companion object {
        fun from(locale: Locale) = LanguageResource(code = locale.isO3Language, displayName = locale.displayName)
    }
}
