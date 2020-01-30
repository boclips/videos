package com.boclips.videos.api.response.contentpartner

import java.util.Locale

data class LanguageResource(
    val code: Locale,
    val name: String
)

fun toLanguageResource(code: Locale): LanguageResource {
    val name = code.displayLanguage
    return LanguageResource(code, name)
}