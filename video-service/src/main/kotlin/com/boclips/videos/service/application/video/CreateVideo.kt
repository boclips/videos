package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.application.video.exceptions.VideoExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
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
    private val classifyVideo: ClassifyVideo,
    private val analyseVideo: AnalyseVideo
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Resource<VideoResource> {
        ensureVideoIsUnique(createRequest.provider!!, createRequest.providerVideoId!!)

        val playbackId = PlaybackId.from(createRequest.playbackId, createRequest.playbackProvider)
        val videoPlayback = findVideoPlayback(playbackId)

        val contentPartner =
            findContentPartner(createRequest) ?: contentPartnerRepository.create(
                ContentPartner(
                    contentPartnerId = createRequest.providerId?.let(::ContentPartnerId)
                        ?: ContentPartnerId(value = ObjectId.get().toHexString()),
                    name = createRequest.provider,
                    ageRange = AgeRange.unbounded(),
                    credit = when (videoPlayback) {
                        is VideoPlayback.YoutubePlayback -> {
                            val metadata =
                                findVideoPlaybackMetadata(playbackId) as VideoProviderMetadata.YoutubeMetadata
                            Credit.YoutubeCredit(channelId = metadata.channelId)
                        }
                        is VideoPlayback.StreamPlayback -> {
                            Credit.PartnerCredit
                        }
                        else -> throw IllegalStateException("Could not retrieve playback for $videoPlayback")
                    },
                    searchable = true
                )
            )

        val videoToBeCreated = createVideoRequestToVideoConverter.convert(createRequest, videoPlayback, contentPartner)
        val createdVideo = videoService.create(videoToBeCreated)


        if (videoToBeCreated.searchable) {
            indexVideo(createdVideo)
        }

        videoCounter.increment()

        if (createRequest.analyseVideo) {
            triggerVideoAnalysis(createdVideo)
        }

        classifyVideo(createdVideo.videoId.value)

        return searchVideo.byId(createdVideo.videoId.value)
    }

    private fun findContentPartner(
        createRequest: CreateVideoRequest
    ): ContentPartner? = when {
        createRequest.providerId != null ->
            contentPartnerRepository.findById(ContentPartnerId(value = createRequest.providerId))
        createRequest.provider != null ->
            contentPartnerRepository.findByName(createRequest.provider)
        else ->
            null
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

    private fun findVideoPlayback(playbackId: PlaybackId): VideoPlayback {
        return playbackRepository.find(playbackId) ?: throw VideoPlaybackNotFound(playbackId)
    }

    private fun findVideoPlaybackMetadata(playbackId: PlaybackId): VideoProviderMetadata? {
        return playbackRepository.getProviderMetadata(playbackId)
    }

    private fun ensureVideoIsUnique(
        contentPartnerName: String,
        contentPartnerVideoId: String
    ) {
        if (videoRepository.existsVideoFromContentPartner(contentPartnerName, contentPartnerVideoId)) {
            throw VideoExists(contentPartnerName, contentPartnerVideoId)
        }
    }
}
