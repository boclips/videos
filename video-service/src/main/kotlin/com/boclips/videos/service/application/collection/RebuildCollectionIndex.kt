package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import mu.KLogging

open class RebuildCollectionIndex(
    private val collectionRepository: CollectionRepository,
    private val collectionSearchService: CollectionSearchService
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        collectionRepository.streamAll { collections ->
            collectionSearchService.safeRebuildIndex(collections, notifier)
        }

        logger.info("Full reindex done")
    }
}
