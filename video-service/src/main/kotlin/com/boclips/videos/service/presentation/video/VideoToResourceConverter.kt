package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import org.springframework.hateoas.Resource

class VideoToResourceConverter(private val videosLinkBuilder: VideosLinkBuilder) {
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
                contentPartner = video.contentPartnerId,
                contentPartnerVideoId = video.contentPartnerVideoId,
                releasedOn = video.releasedOn,
                playback = getPlayback(video),
                subjects = video.subjects.map { it.name }.toSet(),
                badges = getBadges(video),
                type = VideoTypeResource(id = video.type.id, name = video.type.title),
                status = getStatus(video),
                legalRestrictions = video.legalRestrictions,
                hasTranscripts = video.transcript != null
            )
        )
    }

    private fun getPlayback(video: Video): PlaybackResource {
        val playbackResource = when (val playback = video.playback) {
            is StreamPlayback -> StreamPlaybackResource(type = "STREAM", streamUrl = playback.appleHlsStreamUrl)
            is YoutubePlayback -> YoutubePlaybackResource(type = "YOUTUBE")
            else -> throw Exception()
        }
        playbackResource.id = video.playback.id.value
        playbackResource.thumbnailUrl = video.playback.thumbnailUrl
        playbackResource.duration = video.playback.duration
        return playbackResource
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
