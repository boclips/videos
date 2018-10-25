package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.EventEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDateTime

interface EventLogRepository : MongoRepository<EventEntity, String> {

    @Query(value = "{ 'type': ?0, 'timestamp': { '\$gt': ?1 } }", count = true)
    fun countByTypeAfter(type: String, timestamp: LocalDateTime): Long

}
