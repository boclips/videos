package com.boclips.api.presentation

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

open class ApiException : RuntimeException()
class ResourceNotFoundException : ApiException()

@ControllerAdvice
class ErrorHandlerControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handle404(exception: ResourceNotFoundException) = null
}