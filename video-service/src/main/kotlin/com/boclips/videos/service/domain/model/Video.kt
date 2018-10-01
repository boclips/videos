package com.boclips.videos.service.domain.model

import java.time.Duration
import java.time.LocalDate

data class Video(
        val videoId: VideoId,
        val title: String,
        val description: String,
        val duration: Duration,
        val releasedOn: LocalDate,
        val contentProvider: String,
        val videoPlayback: VideoPlayback?
) {
    fun isPlayable(): Boolean {
        return videoPlayback !== null
    }
}