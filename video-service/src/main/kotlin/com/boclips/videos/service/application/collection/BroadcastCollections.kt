package com.boclips.videos.service.application.collection

import com.boclips.contentpartner.service.infrastructure.events.EventsBroadcastProperties
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.collection.CollectionBroadcastRequested
import com.boclips.videos.service.domain.service.events.EventConverter
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import mu.KLogging

class BroadcastCollections(
    private val properties: EventsBroadcastProperties,
    private val collectionRepository: CollectionRepository,
    private val eventBus: EventBus
) {
    companion object : KLogging()

    operator fun invoke() {
        val batchSize = properties.collectionsBatchSize
        val eventConverter = EventConverter()
        collectionRepository.streamAll { collections ->
            collections.windowed(size = batchSize, step = batchSize, partialWindows = true)
                .forEachIndexed { batchIndex, batchOfCollections ->
                    logger.info { "Dispatching collection broadcast events: batch $batchIndex" }
                    val events = batchOfCollections.map { collection ->
                        CollectionBroadcastRequested(eventConverter.toCollectionPayload(collection))
                    }
                    eventBus.publish(events)
                }
        }
    }
}
