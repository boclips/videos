package com.boclips.videos.service.infrastructure.search.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.SearchEvent
import com.boclips.videos.service.presentation.video.SearchResource
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.hateoas.Resource
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Aspect
@Component
class SearchLoggingAspect(
        val searchLogger: SearchLogger
) {

    @Around(
            value = "com.boclips.videos.service.infrastructure.search.event.SearchLoggingPointcuts.searchLoggingAnnotation()"
    )
    fun doAccessCheck(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val result = proceedingJoinPoint.proceed() as Resource<SearchResource>

        return searchLogger.logSearch(result)
    }

}

@Component
class SearchLogger(private val eventService: EventService) {

    fun logSearch(response: Resource<SearchResource>): Resource<SearchResource> {
        val searchId = UUID.randomUUID().toString()
        eventService.saveEvent(SearchEvent(timestamp = ZonedDateTime.now(), searchId = searchId, query = response.content.query, resultsReturned = response.content.videos.size))
        return Resource(response.content.copy(searchId = searchId))
    }

}