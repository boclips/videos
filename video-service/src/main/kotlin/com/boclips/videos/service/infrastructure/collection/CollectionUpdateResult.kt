package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand

data class CollectionUpdateResult(val collection: Collection, val commands: List<CollectionUpdateCommand>)
