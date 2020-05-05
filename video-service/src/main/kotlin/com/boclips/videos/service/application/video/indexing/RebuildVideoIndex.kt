package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoIndex
import mu.KLogging

open class RebuildVideoIndex(
    private val videoRepository: VideoRepository,
    private val videoIndex: VideoIndex
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        videoRepository.streamAll { videos ->
            videoIndex.safeRebuildIndex(videos, notifier)
        }

        logger.info("Full reindex done")
    }
}
