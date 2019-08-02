package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.contentPartner.ContentPartnerNotFoundException
import com.boclips.videos.service.application.exceptions.InvalidCreateRequestException
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.exceptions.VideoExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.video.VideoResource
import io.micrometer.core.instrument.Counter
import mu.KLogging
import org.springframework.hateoas.Resource

class CreateVideo(
    private val videoService: VideoService,
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository,
    private val contentPartnerRepository: ContentPartnerRepository,
    private val searchVideo: SearchVideo,
    private val createVideoRequestToVideoConverter: CreateVideoRequestToVideoConverter,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val videoAnalysisService: VideoAnalysisService,
    private val subjectClassificationService: SubjectClassificationService
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Resource<VideoResource> {
        if (createRequest.providerId == null) {
            throw InvalidCreateRequestException("providerId cannot be null")
        }

        val contentPartner =
            findContentPartner(createRequest)
                ?: throw ContentPartnerNotFoundException("Could not find content partner with id: ${createRequest.providerId}")

        ensureVideoIsUnique(contentPartner, createRequest.providerVideoId!!)

        val playbackId = PlaybackId.from(createRequest.playbackId, createRequest.playbackProvider)
        val videoPlayback = findVideoPlayback(playbackId)

        val subjects = subjectRepository.findByIds(createRequest.subjects ?: emptyList())
        val videoToBeCreated =
            createVideoRequestToVideoConverter.convert(createRequest, videoPlayback, contentPartner, subjects)
        val createdVideo = videoService.create(videoToBeCreated)

        if (createRequest.analyseVideo) {
            triggerVideoAnalysis(createdVideo)
        }

        subjectClassificationService.classifyVideo(createdVideo)

        videoCounter.increment()

        return searchVideo.byId(createdVideo.videoId.value)
    }

    private fun findContentPartner(
        createRequest: CreateVideoRequest
    ): ContentPartner? =
        createRequest.providerId?.let {
            contentPartnerRepository.findById(ContentPartnerId(value = createRequest.providerId))
        }

    private fun triggerVideoAnalysis(createdVideo: Video) {
        try {
            videoAnalysisService.analysePlayableVideo(createdVideo.videoId.value, null)
        } catch (exception: VideoNotAnalysableException) {
            logger.info { "Video cannot be analysed" }
        }
    }

    private fun findVideoPlayback(playbackId: PlaybackId): VideoPlayback {
        return playbackRepository.find(playbackId) ?: throw VideoPlaybackNotFound(playbackId)
    }

    private fun ensureVideoIsUnique(
        contentPartner: ContentPartner,
        contentPartnerVideoId: String
    ) {
        if (videoRepository.existsVideoFromContentPartnerId(
                contentPartner.contentPartnerId.value,
                contentPartnerVideoId
            )
        ) {
            throw VideoExists(contentPartner.name, contentPartnerVideoId)
        }
    }
}
