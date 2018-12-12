package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.EventEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface EventLogRepository : MongoRepository<EventEntity, String>
