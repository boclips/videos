package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.event.InvalidEventException
import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackageClasses = [VideoController::class])
class ExceptionHandling {
    companion object : KLogging()

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Provided query is invalid")
    @ExceptionHandler(SearchRequestValidationException::class)
    fun handleIOException(ex: SearchRequestValidationException) {
        logger.error { "Provided query is invalid $ex" }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested asset does not exist")
    @ExceptionHandler(VideoAssetNotFoundException::class)
    fun handleVideoNotFoundException(ex: VideoAssetNotFoundException) {
        logger.error { "Video not found $ex" }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No or malformed event data was presented")
    @ExceptionHandler(InvalidEventException::class)
    fun handleInvalidEventException(ex: InvalidEventException) {
        logger.error { "Event data malformed or invalid: $ex" }
    }
}
