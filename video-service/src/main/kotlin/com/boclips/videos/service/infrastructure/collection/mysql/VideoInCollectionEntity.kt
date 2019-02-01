package com.boclips.videos.service.infrastructure.collection.mysql

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "collection_video")
class VideoInCollectionEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        var videoId: String? = null,
        var collectionId: String? = null
)