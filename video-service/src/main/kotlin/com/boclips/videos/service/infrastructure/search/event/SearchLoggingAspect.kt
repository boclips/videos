package com.boclips.videos.service.infrastructure.search.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.SearchEvent
import com.boclips.videos.service.presentation.video.SearchResource
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.hateoas.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest


@Aspect
@Component
class SearchLoggingAspect(
        val searchLogger: SearchLogger
) {

    @Around(
            value = "com.boclips.videos.service.infrastructure.search.event.SearchLoggingPointcuts.searchLoggingAnnotation()"
    )
    fun doAccessCheck(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val result = proceedingJoinPoint.proceed() as ResponseEntity<Resource<SearchResource>>

        return searchLogger.logSearch(result.body!!, getCurrentHttpRequest())
    }

    fun getCurrentHttpRequest(): HttpServletRequest? {
        val requestAttributes = RequestContextHolder.getRequestAttributes()
        if (requestAttributes is ServletRequestAttributes) {
            return requestAttributes.request
        }
        return null
    }

}

@Component
class SearchLogger(
        private val eventService: EventService
) {

    fun logSearch(response: Resource<SearchResource>, currentRequest: HttpServletRequest?): ResponseEntity<Resource<SearchResource>> {
        val correlationId = currentRequest?.getHeader("X-Correlation-ID") ?: UUID.randomUUID().toString()

        eventService.saveEvent(SearchEvent(timestamp = ZonedDateTime.now(), correlationId = correlationId, query = response.content.query, resultsReturned = response.content.videos.size))

        val resource = Resource(response.content.copy(searchId = correlationId))

        val headers = HttpHeaders()
        headers["X-Correlation-ID"] = correlationId
        return ResponseEntity(resource, headers, HttpStatus.OK)
    }

}