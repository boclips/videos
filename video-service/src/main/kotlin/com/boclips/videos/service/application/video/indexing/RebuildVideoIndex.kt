package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

open class RebuildVideoIndex(
    private val videoRepository: VideoRepository,
    private val contentPartnerService: ContentPartnerService,
    private val videoSearchService: VideoSearchService
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        videoRepository.streamAll { videos ->
            videos.filter { video ->
                contentPartnerService
                    .findAvailabilityFor(video.contentPartner.contentPartnerId)
                    .isStreamable()
            }.let {
                videoSearchService.safeRebuildIndex(it, notifier)
            }
        }

        logger.info("Full reindex done")
    }
}
