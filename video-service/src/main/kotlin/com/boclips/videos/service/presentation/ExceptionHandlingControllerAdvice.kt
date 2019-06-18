package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.application.analytics.InvalidEventException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackageClasses = [PresentationPackageMarker::class])
class ExceptionHandlingControllerAdvice {
    companion object : KLogging()

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No or malformed event data was presented")
    @ExceptionHandler(InvalidEventException::class)
    fun handleInvalidEventException(ex: InvalidEventException) {
        logger.info { "Event data malformed or invalid: $ex" }
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Not authorized")
    @ExceptionHandler(UnauthorizedException::class)
    fun handleNotAuthorizedException(ex: UnauthorizedException) {
        logger.info { "Unauthorized: $ex" }
    }

}
