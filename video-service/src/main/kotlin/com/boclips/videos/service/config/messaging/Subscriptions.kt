package com.boclips.videos.service.config.messaging

import com.boclips.events.types.Topics.ANALYSED_VIDEOS_SUBSCRIPTION
import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.SubscribableChannel

interface Subscriptions {
    @Input(ANALYSED_VIDEOS_SUBSCRIPTION)
    fun analysedVideos(): SubscribableChannel
}