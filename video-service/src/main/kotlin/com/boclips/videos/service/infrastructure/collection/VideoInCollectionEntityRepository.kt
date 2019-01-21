package com.boclips.videos.service.infrastructure.collection

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
interface VideoInCollectionEntityRepository : CrudRepository<VideoInCollectionEntity, String> {
    fun findByCollectionId(collectionId: String): List<VideoInCollectionEntity>
    fun existsByCollectionIdAndVideoId(collectionId: String, videoId: String): Boolean

    @Transactional
    fun deleteByCollectionIdAndVideoId(collectionId: String, videoId: String)
}