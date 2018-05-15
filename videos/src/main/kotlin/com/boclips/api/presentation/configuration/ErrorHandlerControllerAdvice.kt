package com.boclips.api.presentation.configuration

import com.boclips.api.presentation.IllegalFilterException
import com.boclips.api.presentation.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ErrorHandlerControllerAdvice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handle404(exception: ResourceNotFoundException) = null

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle400(exception: IllegalFilterException) = null
}