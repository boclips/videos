package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import mu.KLogging

open class RebuildCollectionIndex(
    private val collectionRepository: CollectionRepository,
    private val collectionIndex: CollectionIndex
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        collectionRepository.streamAll { collections ->
            collectionIndex.safeRebuildIndex(collections, notifier)
        }

        logger.info("Full reindex done")
    }
}
