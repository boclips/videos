package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import mu.KLogging

open class RebuildLegacySearchIndex(
    private val videoRepository: VideoRepository,
    private val contentPartnerService: ContentPartnerService,
    private val legacyVideoSearchService: LegacyVideoSearchService
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Building a legacy index")

        videoRepository.streamAll { videos ->
            val filteredVideos = videos
                .filter { it.title.isNotEmpty() }
                .filter { it.isPlayable() }
                .filter { it.isBoclipsHosted() }
                .filter {
                    contentPartnerService
                        .findAvailabilityFor(it.contentPartner.contentPartnerId)
                        .isDownloadable()
                }
                .map(VideoToLegacyVideoMetadataConverter::convert)

            legacyVideoSearchService.upsert(filteredVideos, notifier)
        }

        logger.info("Building a legacy index done.")
    }
}
