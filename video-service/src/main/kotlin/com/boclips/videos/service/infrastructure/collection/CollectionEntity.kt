package com.boclips.videos.service.infrastructure.collection

import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "collection")
class CollectionEntity(
        @Id var id: String? = null,
        var owner: String? = null,
        var title: String? = null
)