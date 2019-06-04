package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
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
        return videoIds.map { videoId -> wrapResourceWithHateoas(VideoResource(id = videoId.value)) }
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
                contentPartner = video.owner.contentPartner.name,
                contentPartnerVideoId = video.owner.videoReference,
                releasedOn = video.releasedOn,
                playback = getPlayback(video),
                subjects = video.subjects.map { it.name }.toSet(),
                badges = getBadges(video),
                type = VideoTypeResource(id = video.type.id, name = video.type.title),
                status = getStatus(video),
                legalRestrictions = video.legalRestrictions,
                hasTranscripts = video.transcript != null,
                ageRange = getAgeRange(video)
            )
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

    private fun getStatus(video: Video): VideoResourceStatus {
        return if (video.searchable) {
            VideoResourceStatus.SEARCHABLE
        } else {
            VideoResourceStatus.SEARCH_DISABLED
        }
    }

    private fun wrapResourceWithHateoas(
        videoResource: VideoResource
    ) = Resource(
        videoResource,
        listOfNotNull(
            videosLinkBuilder.self(videoResource),
            videosLinkBuilder.transcriptLink(videoResource)
        )
    )
}
