package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.service.collection.CollectionReadService
import com.boclips.videos.service.presentation.projections.Projection

class GetCollection(private val collectionReadService: CollectionReadService) {
    operator fun invoke(collectionId: String, projection: Projection? = Projection.list, user: User): Collection {
        return collectionReadService.find(CollectionId(value = collectionId), user)
            ?: throw CollectionNotFoundException(collectionId)
    }
}
