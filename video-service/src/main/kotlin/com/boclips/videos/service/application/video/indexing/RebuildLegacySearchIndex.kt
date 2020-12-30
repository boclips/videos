package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.VideoChannelService
import com.boclips.videos.service.infrastructure.video.converters.VideoToLegacyVideoMetadataConverter
import mu.KLogging

open class RebuildLegacySearchIndex(
    private val videoRepository: VideoRepository,
    private val videoChannelService: VideoChannelService,
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
                    videoChannelService
                        .findAvailabilityFor(it.channel.channelId)
                        .isDownloadable()
                }
                .map(VideoToLegacyVideoMetadataConverter::convert)

            legacyVideoSearchService.upsert(filteredVideos, notifier)
        }

        logger.info("Building a legacy index done.")
    }
}
