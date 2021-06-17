package com.boclips.videos.service.domain.model.video

import java.util.Locale

sealed class Voice {
    abstract val transcript: String?
    abstract val language: Locale?
    abstract val isTranscriptHumanGenerated: Boolean?

    object WithoutVoice : Voice() {
        override val transcript: String? = null
        override val language: Locale? = null
        override val isTranscriptHumanGenerated: Boolean? = null
    }

    data class WithVoice(
        override val transcript: String?,
        override val language: Locale?,
        override val isTranscriptHumanGenerated: Boolean?
    ) : Voice()

    data class UnknownVoice(
        override val transcript: String?,
        override val language: Locale?,
        override val isTranscriptHumanGenerated: Boolean?
    ) : Voice()

    fun isVoiced(): Boolean? = when (this) {
        WithoutVoice -> false
        is WithVoice -> true
        is UnknownVoice -> null
    }
}
