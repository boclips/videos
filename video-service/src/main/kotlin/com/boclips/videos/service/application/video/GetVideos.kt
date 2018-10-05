package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.PlaybackService
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.hateoas.Resource

class GetVideos(
        private val videoService: VideoService,
        private val playbackService: PlaybackService,
        private val videoToResourceConverter: VideoToResourceConverter
) {
    fun get(videoId: String): VideoResource {
        val videoWithoutPlayback = videoService.findVideoBy(VideoId(videoId = videoId))
        val videoWithPlayback: Video = playbackService.getVideoWithPlayback(videoWithoutPlayback)

        return videoToResourceConverter.convert(videoWithPlayback)
    }

    fun get(query: String?): List<Resource<VideoResource>> {
        query ?: throw QueryValidationException()

        val videosWithoutPlayback = videoService.findVideosBy(VideoSearchQuery(text = query))
        val playableVideos = playbackService.getVideosWithPlayback(videosWithoutPlayback)

        return videoToResourceConverter.convert(playableVideos).map { videoResource: VideoResource ->
            Resource(videoResource, VideoController.getVideoLink(videoResource.id, "self"))
        }
    }
}