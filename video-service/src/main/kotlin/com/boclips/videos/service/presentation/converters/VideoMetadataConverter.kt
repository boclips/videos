package com.boclips.videos.service.presentation.converters

import com.boclips.search.service.domain.videos.model.VideoMetadataToCSV
import com.boclips.videos.api.request.video.CaptionFormatRequest
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder

class VideoMetadataConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
) {
    fun convert(
        videos: List<VideoResource>,
        videoToCaptionLinkMap: Map<String, Caption?>
    ): List<VideoMetadataToCSV> {
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
                downloadSrtCaptionUrl = videoToCaptionLinkMap[it.id]?.let { _ ->
                    videosLinkBuilder.downloadCaption(it.id, CaptionFormatRequest.SRT)?.href
                },
                downloadVttCaptionUrl = videoToCaptionLinkMap[it.id]?.let { _ ->
                    videosLinkBuilder.downloadCaption(it.id, CaptionFormatRequest.VTT)?.href
                },
            )
        }
    }
}
