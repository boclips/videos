package com.boclips.videos.service.application.video

import com.boclips.contentpartner.service.application.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.application.exceptions.InvalidCreateRequestException
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoNotCreatedException
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToVideoConverter
import io.micrometer.core.instrument.Counter
import mu.KLogging

class CreateVideo(
    private val videoService: VideoService,
    private val subjectRepository: SubjectRepository,
    private val contentPartnerService: ContentPartnerService,
    private val searchVideo: SearchVideo,
    private val createVideoRequestToVideoConverter: CreateVideoRequestToVideoConverter,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val videoAnalysisService: VideoAnalysisService,
    private val subjectClassificationService: SubjectClassificationService
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Video {
        if (createRequest.providerId == null) {
            throw InvalidCreateRequestException("providerId cannot be null")
        }

        val contentPartner = findContentPartner(createRequest)
            ?: throw ContentPartnerNotFoundException("Could not find content partner with id: ${createRequest.providerId}")

        val distributionMethods = findDistributionMethods(contentPartner.contentPartnerId)

        val playbackId = PlaybackId.from(createRequest.playbackId, createRequest.playbackProvider)
        val videoPlayback = findVideoPlayback(playbackId)
        val subjects = subjectRepository.findByIds(createRequest.subjects ?: emptyList())

        val videoToBeCreated =
            createVideoRequestToVideoConverter.convert(createRequest, videoPlayback, contentPartner, distributionMethods, subjects)

        val createdVideo = try {
            videoService.create(videoToBeCreated)
        } catch (ex: VideoNotCreatedException) {
            throw VideoAssetAlreadyExistsException(
                ex.video.contentPartner.name,
                ex.video.videoReference
            )
        }

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
            contentPartnerService.findById(createRequest.providerId)
        }

    private fun findDistributionMethods(contentPartnerId: ContentPartnerId): Set<DistributionMethod> {
        return contentPartnerService.getDistributionMethods(contentPartnerId) ?: emptySet()
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
}
