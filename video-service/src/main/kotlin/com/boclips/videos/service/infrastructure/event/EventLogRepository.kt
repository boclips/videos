package com.boclips.videos.service.infrastructure.event

import org.springframework.data.mongodb.repository.MongoRepository

interface EventLogRepository : MongoRepository<Event<*>, String>