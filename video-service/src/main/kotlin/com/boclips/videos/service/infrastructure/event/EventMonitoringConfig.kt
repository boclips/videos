package com.boclips.videos.service.infrastructure.event

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "event.monitoring")
data class EventMonitoringConfig(val lookbackHours: LookbackHours)