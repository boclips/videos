package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.video.exceptions.VideoExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.video.VideoResource
import io.micrometer.core.instrument.Counter
import mu.KLogging
import org.springframework.hateoas.Resource

class CreateVideo(
    private val videoService: VideoService,
    private val videoRepository: VideoRepository,
    private val searchVideo: SearchVideo,
    private val createVideoRequestToVideoConverter: CreateVideoRequestToVideoConverter,
    private val searchServiceAdmin: SearchService,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val legacySearchService: LegacySearchService,
    private val analyseVideo: AnalyseVideo
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Resource<VideoResource> {
        val videoPlayback = findVideoPlayback(createRequest)
        val videoToBeCreated = createVideoRequestToVideoConverter.convert(createRequest, videoPlayback)
        ensureVideoIsUnique(videoToBeCreated)
        val createdVideo = videoService.create(videoToBeCreated)

        searchServiceAdmin.upsert(sequenceOf(createdVideo), null)

        if (videoToBeCreated.isBoclipsHosted()) {
            legacySearchService.upsert(sequenceOf(VideoToLegacyVideoMetadataConverter.convert(createdVideo)), null)
        }

        if (createRequest.analyseVideo) {
            try {
                analyseVideo(createdVideo.videoId.value, null)
            } catch (exception: VideoNotAnalysableException) {
                logger.info { "Video cannot be analysed" }
            }
        }

        videoCounter.increment()
        return searchVideo.byId(createdVideo.videoId.value)
    }

    private fun findVideoPlayback(createRequest: CreateVideoRequest): VideoPlayback {
        return playbackRepository.find(
            PlaybackId.from(
                createRequest.playbackId,
                createRequest.playbackProvider
            )
        ) ?: throw VideoPlaybackNotFound(createRequest)
    }

    private fun ensureVideoIsUnique(video: Video) {
        if (videoRepository.existsVideoFromContentPartner(video.contentPartnerName, video.contentPartnerVideoId)) {
            throw VideoExists(video.contentPartnerName, video.contentPartnerVideoId)
        }
    }
}
