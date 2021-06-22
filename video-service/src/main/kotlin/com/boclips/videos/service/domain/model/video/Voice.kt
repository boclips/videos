package com.boclips.videos.service.domain.model.video

import java.util.Locale

sealed class Voice {
    abstract val transcript: Transcript?
    abstract val language: Locale?

    object WithoutVoice : Voice() {
        override val transcript: Transcript? = null
        override val language: Locale? = null
    }

    data class WithVoice(
        override val transcript: Transcript?,
        override val language: Locale?,
    ) : Voice()

    data class UnknownVoice(
        override val transcript: Transcript?,
        override val language: Locale?,
    ) : Voice()

    fun isVoiced(): Boolean? = when (this) {
        WithoutVoice -> false
        is WithVoice -> true
        is UnknownVoice -> null
    }
}
