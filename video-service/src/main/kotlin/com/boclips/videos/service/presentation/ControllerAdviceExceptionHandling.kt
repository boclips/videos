package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.UnauthorizedException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackageClasses = [PresentationPackageMarker::class])
class ControllerAdviceExceptionHandling {
    companion object : KLogging()

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Not authorized")
    @ExceptionHandler(UnauthorizedException::class)
    fun handleNotAuthorizedException(ex: UnauthorizedException) {
        logger.info { "Unauthorized: $ex" }
    }
}
