package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.exceptions.QueryValidationException
import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.presentation.video.VideoController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackageClasses = [VideoController::class])
class ExceptionHandling {
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Provided query is invalid")
    @ExceptionHandler(QueryValidationException::class)
    fun handleIOException() {
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested video does not exist")
    @ExceptionHandler(VideoNotFoundException::class)
    fun handleVideoNotFoundException() {
    }
}
