package com.boclips.videos.service.application.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository

class GetContractedCollections(private val collectionRepository: CollectionRepository) {
    operator fun invoke(collectionFilter: CollectionFilter, contracts: List<Contract>): Page<Collection> {
        return collectionRepository.getByContracts(
            contracts,
            PageRequest(page = collectionFilter.pageNumber, size = collectionFilter.pageSize)
        )
    }
}
