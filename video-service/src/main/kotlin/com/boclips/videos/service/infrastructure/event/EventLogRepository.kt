package com.boclips.videos.service.infrastructure.event

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDateTime

interface EventLogRepository : MongoRepository<Event<*>, String> {

    @Query(value = "{ 'type': ?0, 'timestamp': { '\$gt': ?1 } }", count = true)
    fun countByTypeAfter(type: String, timestamp: LocalDateTime): Long

}
