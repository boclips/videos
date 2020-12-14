package com.boclips.videos.service.presentation.converters

import com.boclips.search.service.domain.videos.model.VideoMetadataToCSV
import com.boclips.videos.api.response.video.VideoResource

object VideoMetadataConverter {
    fun convert(videos: List<VideoResource>): List<VideoMetadataToCSV> {
        return videos.map { it ->
            VideoMetadataToCSV(
                id = it.id,
                contentPartner = it.channel,
                title = it.title,
                description = it.description,
                duration = it.playback!!.duration.toString(),
                videoReference = it.channelVideoId,
                legalRestrictions = it.legalRestrictions,
                transcripts = it.hasTranscripts,
                links = it.playback!!._links,
                keywords = it.bestFor?.map { it.label }
            )
        }
    }
}
