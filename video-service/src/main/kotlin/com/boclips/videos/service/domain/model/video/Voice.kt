package com.boclips.videos.service.domain.model.video

import java.util.Locale

sealed class Voice {
    abstract val transcript: String? // create a Transcript to group the 3 concepts?
    abstract val language: Locale?
    abstract val isTranscriptHumanGenerated: Boolean?
    abstract val isTranscriptRequested: Boolean?

    object WithoutVoice : Voice() {
        override val transcript: String? = null
        override val language: Locale? = null
        override val isTranscriptHumanGenerated: Boolean? = null
        override val isTranscriptRequested: Boolean? = false
    }

    data class WithVoice(
        override val transcript: String?,
        override val language: Locale?,
        override val isTranscriptHumanGenerated: Boolean?,
        override val isTranscriptRequested: Boolean? = false
    ) : Voice()

    data class UnknownVoice(
        override val transcript: String?,
        override val language: Locale?,
        override val isTranscriptHumanGenerated: Boolean?,
        override val isTranscriptRequested: Boolean? = false
    ) : Voice()

    fun isVoiced(): Boolean? = when (this) {
        WithoutVoice -> false
        is WithVoice -> true
        is UnknownVoice -> null
    }
}
