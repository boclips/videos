package com.boclips.videos.service.application.video

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class BulkVideoSearchUpdate(
    val contentPartnerRepository: ContentPartnerRepository,
    val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService,
    private val legacyVideoSearchService: LegacyVideoSearchService
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEOS_EXCLUSION_FROM_STREAM_REQUESTED)
    operator fun invoke(event: VideosExclusionFromStreamRequested) {
        try {
            videoSearchService.bulkRemoveFromSearch(event.videoIds)
        } catch (e: Exception) {
            logger.info { "Could not exclude from search for STREAM because ${e.message}" }
        }
    }

    @StreamListener(Subscriptions.VIDEOS_EXCLUSION_FROM_DOWNLOAD_REQUESTED)
    operator fun invoke(event: VideosExclusionFromDownloadRequested) {
        try {
            legacyVideoSearchService.bulkRemoveFromSearch(event.videoIds)
        } catch (e: Exception) {
            logger.info { "Could not exclude from search for DOWNLOAD because ${e.message}" }
        }
    }

    @StreamListener(Subscriptions.VIDEOS_INCLUSION_IN_STREAM_REQUESTED)
    operator fun invoke(event: VideosInclusionInStreamRequested) {
        try {
            val videos = videoRepository.findAll(event.videoIds.map { VideoId(value = it) })
            videoSearchService.upsert(videos.asSequence())
        } catch (e: Exception) {
            logger.info { "Could not include in search from STREAM because ${e.message}" }
        }
    }

    @StreamListener(Subscriptions.VIDEOS_INCLUSION_IN_DOWNLOAD_REQUESTED)
    operator fun invoke(event: VideosInclusionInDownloadRequested) {
        try {
            val videos = videoRepository.findAll(event.videoIds.map { VideoId(value = it) })
            legacyVideoSearchService.upsert(videos
                .filter { it.isBoclipsHosted() }
                .map { video -> VideoToLegacyVideoMetadataConverter.convert(video) }
                .asSequence())
        } catch (e: Exception) {
            logger.info { "Could not include in search from DOWNLOAD because ${e.message}" }
        }
    }
}
