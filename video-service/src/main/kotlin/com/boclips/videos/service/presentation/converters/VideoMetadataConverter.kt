package com.boclips.videos.service.presentation.converters

import com.boclips.search.service.domain.videos.model.VideoMetadataToCSV
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoUrlAssetsResource

object VideoMetadataConverter {
    fun convert(videos: List<VideoResource>, videoToCaptionLinkMap: Map<String, VideoUrlAssetsResource>): List<VideoMetadataToCSV> {
        return videos.map {
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
                keywords = it.keywords,
                downloadCaptionUrl = videoToCaptionLinkMap[it.id]!!.downloadCaptionUrl,
                captionFileExtension = videoToCaptionLinkMap[it.id]!!.captionFileExtension,
            )
        }
    }
}
