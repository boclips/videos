package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.video.exceptions.VideoExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToVideoConverter
import com.boclips.videos.service.presentation.video.VideoResource
import io.micrometer.core.instrument.Counter
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.hateoas.Resource

class CreateVideo(
    private val videoService: VideoService,
    private val videoRepository: VideoRepository,
    private val contentPartnerRepository: ContentPartnerRepository,
    private val searchVideo: SearchVideo,
    private val createVideoRequestToVideoConverter: CreateVideoRequestToVideoConverter,
    private val videoSearchServiceAdmin: VideoSearchService,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val legacySearchService: LegacySearchService,
    private val analyseVideo: AnalyseVideo
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Resource<VideoResource> {
        ensureVideoIsUnique(createRequest.provider!!, createRequest.providerVideoId!!)

        val videoPlayback = findVideoPlayback(createRequest)
        val contentPartner = createOrFindContentPartner(createRequest.provider)
        val videoToBeCreated = createVideoRequestToVideoConverter.convert(createRequest, videoPlayback, contentPartner)
        val createdVideo = videoService.create(videoToBeCreated)

        indexVideo(createdVideo)
        videoCounter.increment()

        if (createRequest.analyseVideo) {
            triggerVideoAnalysis(createdVideo)
        }

        return searchVideo.byId(createdVideo.videoId.value)
    }

    private fun triggerVideoAnalysis(createdVideo: Video) {
        try {
            analyseVideo(createdVideo.videoId.value, null)
        } catch (exception: VideoNotAnalysableException) {
            logger.info { "Video cannot be analysed" }
        }
    }

    private fun indexVideo(createdVideo: Video) {
        videoSearchServiceAdmin.upsert(sequenceOf(createdVideo), null)

        if (createdVideo.isBoclipsHosted()) {
            legacySearchService.upsert(sequenceOf(VideoToLegacyVideoMetadataConverter.convert(createdVideo)), null)
        }
    }

    private fun findVideoPlayback(createRequest: CreateVideoRequest): VideoPlayback {
        return playbackRepository.find(
            PlaybackId.from(
                createRequest.playbackId,
                createRequest.playbackProvider
            )
        ) ?: throw VideoPlaybackNotFound(createRequest)
    }

    private fun ensureVideoIsUnique(
        contentPartnerName: String,
        contentPartnerVideoId: String
    ) {
        if (videoRepository.existsVideoFromContentPartner(contentPartnerName, contentPartnerVideoId)) {
            throw VideoExists(contentPartnerName, contentPartnerVideoId)
        }
    }

    private fun createOrFindContentPartner(provider: String): ContentPartner {
        val existingContentPartner = contentPartnerRepository.findByName(provider)

        if (existingContentPartner == null) {
            logger.info { "Create new content partner $provider" }
            val contentPartner = ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = provider,
                ageRange = null
            )

            return contentPartnerRepository.create(contentPartner)
        }

        return existingContentPartner
    }
}
