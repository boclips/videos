package com.boclips.videos.service.presentation.converters

import com.boclips.search.service.domain.videos.model.VideoMetadataToCSV
import com.boclips.videos.service.domain.model.video.Video

object VideoMetadataConverter {
    fun convert(videos: List<Video>): List<VideoMetadataToCSV> {
        return videos.map { it ->
            VideoMetadataToCSV(
                id = it.videoId.value,
                contentPartner = it.channel.name,
                title = it.title,
                description = it.description,
                duration = it.playback.duration.toString(),
                videoReference = it.videoReference,
                legalRestrictions = it.legalRestrictions,
                transcripts = it.hasTranscript(),
                keywords = it.keywords
            )
        }
    }
}
