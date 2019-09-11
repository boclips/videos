package com.boclips.videos.service.domain.service

import com.boclips.users.client.model.contract.Contract
import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.domain.model.collection.Collection

class IsContractedToView {
    operator fun invoke(collection: Collection, contracts: List<Contract>): Boolean {
        return contracts.any {
            when (it) {
                is SelectedContentContract -> it.collectionIds.contains(collection.id.value)
                else -> false
            }
        }
    }
}