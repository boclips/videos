package com.boclips.videos.service.infrastructure.logging

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.SearchEvent
import com.boclips.videos.service.infrastructure.event.types.User
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.hateoas.Resources
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
        value = "com.boclips.videos.service.infrastructure.logging.SearchLoggingPointcuts.searchLoggingAnnotation()"
    )
    fun doAccessCheck(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val result = proceedingJoinPoint.proceed() as ResponseEntity<Resources<*>>
        val query = proceedingJoinPoint.args[0].toString()

        return searchLogger.logSearch(
            result.body!!,
            getCurrentHttpRequest(),
            User.fromSecurityUser(UserExtractor.getCurrentUser()),
            query
        )
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
    companion object {
        const val X_CORRELATION_ID = "X-Correlation-ID"
    }

    fun logSearch(
        response: Resources<*>,
        currentRequest: HttpServletRequest?,
        user: User,
        query: String
    ): ResponseEntity<Resources<*>> {
        val correlationId = currentRequest?.getHeader(X_CORRELATION_ID) ?: UUID.randomUUID().toString()

        eventService.saveEvent(
            SearchEvent(
                timestamp = ZonedDateTime.now(),
                correlationId = correlationId,
                query = query,
                resultsReturned = response.content.size,
                user = user
            )
        )

        val headers = HttpHeaders()
        headers[X_CORRELATION_ID] = correlationId
        return ResponseEntity(response, headers, HttpStatus.OK)
    }
}