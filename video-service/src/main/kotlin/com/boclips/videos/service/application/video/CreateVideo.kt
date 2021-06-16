package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.service.application.GetCategoryWithAncestors
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.subject.SubjectClassificationService
import com.boclips.videos.service.application.video.exceptions.ChannelNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoAssetAlreadyExistsException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.channel.FallbackMetadata
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
    private val subjectClassificationService: SubjectClassificationService,
    private val getCategoryWithAncestors: GetCategoryWithAncestors,
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest, user: User): Video {
        logger.info { "Received video creation request for video ${createRequest.providerId}: $createRequest" }

        val fallbackMetadata = findFallbackMetadata(createRequest)
            ?: throw ChannelNotFoundException(
                "Could not find channel with id: ${createRequest.providerId}"
            )

        val playbackId = PlaybackId.from(createRequest.playbackId, createRequest.playbackProvider)
        val videoPlayback = findVideoPlayback(playbackId)
        val subjects = subjectRepository.findByIds(createRequest.subjects ?: emptyList())

        val categories = mapOf(
            CategorySource.MANUAL to (createRequest.categories?.map { getCategoryWithAncestors(it) }?.toSet()
                ?: emptySet()),
            CategorySource.CHANNEL to (fallbackMetadata.categories ?: emptySet())
        )

        logger.info { "Obtained video playback and subjects for video ${createRequest.providerId}" }

        val videoToBeCreated =
            createVideoRequestToVideoConverter.convert(
                createVideoRequest = createRequest,
                videoPlayback = videoPlayback,
                channel = fallbackMetadata.channel,
                subjects = subjects,
                categories = categories,
                fallbackLanguage = fallbackMetadata.language
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

    private fun findFallbackMetadata(createRequest: CreateVideoRequest): FallbackMetadata? =
        createRequest.providerId?.let {
            videoChannelService.findFallbackMetadata(it)
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
