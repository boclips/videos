package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.model.video.PriceComputingService
import com.boclips.videos.service.domain.model.video.prices.VideoWithPrices
import com.boclips.videos.service.domain.service.OrganisationService
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.domain.service.video.VideoRepository
import mu.KLogging

open class RebuildVideoIndex(
    private val videoRepository: VideoRepository,
    private val videoIndex: VideoIndex,
    private val organisationService: OrganisationService,
    private val priceComputingService: PriceComputingService
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        val organisationsWithPrices = organisationService.getOrganisationsWithCustomPrices()

        videoRepository.streamAll { videos ->
            val hydratedVideos = videos.map { video ->
                val videoPrices = priceComputingService.computeVideoOrganisationPrices(
                    channel = video.channel.channelId,
                    playback = video.playback,
                    videoTypes = video.types,
                    videoId = video.videoId,
                    organisationsPrices = organisationsWithPrices
                )
                videoPrices?.let { VideoWithPrices(video = video, prices = it) } ?: video
            }

            videoIndex.safeRebuildIndex(hydratedVideos, notifier)
        }

        logger.info("Full reindex done")
    }
}
