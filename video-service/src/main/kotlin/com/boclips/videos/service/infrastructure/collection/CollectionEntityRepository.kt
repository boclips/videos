package com.boclips.videos.service.infrastructure.collection

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
interface CollectionEntityRepository : CrudRepository<CollectionEntity, String> {
    fun findByOwner(owner: String): List<CollectionEntity>
}