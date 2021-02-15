package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.exceptions.ChannelNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoCreationService
import com.boclips.videos.service.domain.service.video.VideoNotCreatedException
import com.boclips.videos.service.presentation.converters.CreateVideoRequestToVideoConverter
import io.micrometer.core.instrument.Counter
import mu.KLogging

class CreateVideo(
    private val videoCreationService: VideoCreationService,
    private val subjectRepository: SubjectRepository,
    private val videoChannelService: VideoChannelService,
    private val createVideoRequestToVideoConverter: CreateVideoRequestToVideoConverter,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val videoAnalysisService: VideoAnalysisService,
    private val subjectClassificationService: SubjectClassificationService
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest, user: User): Video {
        logger.info { "Received video creation request for video ${createRequest.providerId}: $createRequest" }

        val channel = findChannel(createRequest)
            ?: throw ChannelNotFoundException(
                "Could not find channel with id: ${createRequest.providerId}"
            )

        val playbackId = PlaybackId.from(createRequest.playbackId, createRequest.playbackProvider)
        val videoPlayback = findVideoPlayback(playbackId)
        val subjects = subjectRepository.findByIds(createRequest.subjects ?: emptyList())

        logger.info { "Obtained video playback and subjects for video ${createRequest.providerId}" }

        val videoToBeCreated =
            createVideoRequestToVideoConverter.convert(
                createVideoRequest = createRequest,
                videoPlayback = videoPlayback,
                contentPartner = channel,
                subjects = subjects
            )

        val createdVideo = try {
            videoCreationService.create(videoToBeCreated, user)
        } catch (ex: VideoNotCreatedException) {
            throw VideoAssetAlreadyExistsException(ex.video.channel.name, ex.video.videoReference)
        }

        logger.info { "Successfully created video ${createRequest.providerId}" }

        if (createRequest.analyseVideo) {
            triggerVideoAnalysis(createdVideo)
        }

        subjectClassificationService.classifyVideo(createdVideo)

        videoCounter.increment()

        return createdVideo
    }

    private fun findChannel(createRequest: CreateVideoRequest): Channel? =
        createRequest.providerId?.let {
            videoChannelService.findById(createRequest.providerId!!)
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
