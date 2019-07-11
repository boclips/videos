package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import org.springframework.hateoas.Resource
import org.springframework.stereotype.Component

@Component
class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter
) {
    fun wrapVideosInResource(videos: List<Video>): List<Resource<VideoResource>> {
        return videos.map { video -> fromVideo(video) }
    }

    fun wrapVideoIdsInResource(videoIds: List<VideoId>): List<Resource<VideoResource>> {
        return videoIds.map { videoId -> wrapResourceWithHateoas(VideoResource(id = videoId.value), null) }
    }

    fun fromVideo(video: Video): Resource<VideoResource> {
        return toResource(video)
    }

    private fun toResource(video: Video): Resource<VideoResource> {
        return wrapResourceWithHateoas(
            VideoResource(
                id = video.videoId.value,
                title = video.title,
                description = video.description,
                source = video.contentPartner.name,
                contentPartner = video.contentPartner.name,
                contentPartnerVideoId = video.videoReference,
                releasedOn = video.releasedOn,
                playback = getPlayback(video),
                subjects = video.subjects.map { it.name }.toSet(),
                badges = getBadges(video),
                type = VideoTypeResource(id = video.type.id, name = video.type.title),
                legalRestrictions = video.legalRestrictions,
                hasTranscripts = video.transcript != null,
                ageRange = getAgeRange(video),
                rating = video.getRatingAverage(),
                hiddenFromSearchForDeliveryMethods = video.hiddenFromSearchForDistributionMethods.map(
                    DeliveryMethodResourceConverter::toResource
                ).toSet()
            ), video
        )
    }

    private fun getAgeRange(video: Video): AgeRangeResource? {
        return AgeRangeToResourceConverter.convert(video.ageRange)
    }

    private fun getPlayback(video: Video): Resource<PlaybackResource> {
        return playbackToResourceConverter.wrapPlaybackInResource(video.playback)
    }

    private fun getBadges(video: Video): Set<String> {
        return when (video.playback) {
            is YoutubePlayback -> setOf(VideoBadge.YOUTUBE.id)
            else -> setOf(VideoBadge.AD_FREE.id)
        }
    }

    private fun wrapResourceWithHateoas(
        videoResource: VideoResource,
        video: Video?
    ) = Resource(
        videoResource,
        listOfNotNull(
            videosLinkBuilder.self(videoResource),
            videosLinkBuilder.transcriptLink(videoResource),
            video?.let { videosLinkBuilder.rateLink(it) }
        )
    )
}
