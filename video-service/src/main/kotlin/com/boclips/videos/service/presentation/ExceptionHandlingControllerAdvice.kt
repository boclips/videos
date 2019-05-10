package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.UnauthorizedException
import com.boclips.videos.service.application.analytics.InvalidEventException
import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackageClasses = [PresentationPackageMarker::class])
class ExceptionHandlingControllerAdvice {
    companion object : KLogging()

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Provided query is invalid")
    @ExceptionHandler(SearchRequestValidationException::class)
    fun handleIOException(ex: SearchRequestValidationException) {
        logger.info { "Provided query is invalid $ex" }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested asset does not exist")
    @ExceptionHandler(VideoAssetNotFoundException::class)
    fun handleVideoNotFoundException(ex: VideoAssetNotFoundException) {
        logger.info { "Video not found $ex" }
    }

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

    @ExceptionHandler(InvalidSourceException::class)
    fun handleInvalidSourceException(ex: InvalidSourceException): ResponseEntity<String> {
        logger.info { "Invalid source: $ex" }

        return ResponseEntity
            .badRequest()
            .body(ex.message)
    }

    @ExceptionHandler(InvalidDurationException::class)
    fun handleInvalidDurationException(ex: InvalidDurationException): ResponseEntity<String> {
        logger.info { "Invalid duration: $ex" }

        return ResponseEntity
            .badRequest()
            .body(ex.message)
    }

    @ExceptionHandler(InvalidDateException::class)
    fun handleInvalidDateException(ex: InvalidDateException): ResponseEntity<String> {
        logger.info { " Invalid date: $ex" }

        return ResponseEntity.badRequest().body(ex.message)
    }
}
