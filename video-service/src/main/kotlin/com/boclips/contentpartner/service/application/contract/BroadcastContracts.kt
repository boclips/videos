package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contract.BroadcastContractRequested
import mu.KLogging

class BroadcastContracts(
    private val eventBus: EventBus,
    private val eventConverter: EventConverter,
    private val contractRepository: ContractRepository
) {
    operator fun invoke() {
        val batchSize = 500
        contractRepository.streamAll { contracts ->
            contracts.windowed(size = batchSize, step = batchSize, partialWindows = true)
                .forEachIndexed { batchIndex, contractBatch ->
                    logger.info { "Dispatching contract broadcast events: batch $batchIndex" }
                    val events = contractBatch.map { contract ->
                        BroadcastContractRequested.builder()
                            .contract(eventConverter.toContractPayload(contract))
                            .build()
                    }
                    eventBus.publish(events)
                }
        }
    }

    companion object : KLogging()
}
