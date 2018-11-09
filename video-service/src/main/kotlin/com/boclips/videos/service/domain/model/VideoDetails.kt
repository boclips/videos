package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.playback.PlaybackId
import java.time.LocalDate

data class VideoDetails(
        val videoId: VideoId,
        val playbackId: PlaybackId,
        val title: String,
        val description: String,
        val keywords: List<String>,
        val releasedOn: LocalDate,
        val contentProvider: String,
        val type: VideoType
)