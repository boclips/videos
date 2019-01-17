package com.boclips.videos.service.infrastructure.collection

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
interface VideoInCollectionEntityRepository : CrudRepository<VideoInCollectionEntity, String> {
    fun findByCollectionId(collectionId: String): List<VideoInCollectionEntity>
}