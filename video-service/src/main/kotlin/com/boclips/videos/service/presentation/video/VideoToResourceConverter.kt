package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.presentation.video.playback.PlaybackResource
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource

class VideoToResourceConverter {
    fun convert(videos: List<Video>): List<VideoResource> {
        return videos.map { video -> convert(video) }
    }

    fun convert(video: Video): VideoResource {
        return toResource(video)
    }

    private fun toResource(video: Video): VideoResource {
        return VideoResource(
                id = video.asset.assetId.value,
                title = video.asset.title,
                description = video.asset.description,
                contentPartner = video.asset.contentPartnerId,
                contentPartnerVideoId = video.asset.contentPartnerVideoId,
                releasedOn = video.asset.releasedOn,
                playback = getPlayback(video),
                subjects = video.asset.subjects.map { it.name }.toSet(),
                badges = getBadges(video),
                type = VideoTypeResource(id = video.asset.type.id, name = video.asset.type.title)
        )
    }

    private fun getPlayback(video: Video): PlaybackResource {
        val playback = video.playback
        val playbackResource = when (playback) {
            is StreamPlayback -> StreamPlaybackResource(type = "STREAM", streamUrl = playback.streamUrl)
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
}