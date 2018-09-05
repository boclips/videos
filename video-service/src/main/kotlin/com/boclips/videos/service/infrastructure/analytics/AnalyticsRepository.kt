package com.boclips.videos.service.infrastructure.analytics

import org.springframework.data.mongodb.repository.MongoRepository

interface AnalyticsRepository : MongoRepository<AnalyticsEvent<*>, String>