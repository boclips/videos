package com.boclips.videos.service.infrastructure.event

class EventService(private val eventLogRepository: EventLogRepository) {

    fun <T> saveEvent(event: Event<T>) {
        eventLogRepository.insert(event)
    }
}