package com.boclips.contentpartner.service.presentation

import com.boclips.videos.service.presentation.projections.ProjectionResolver
import org.springframework.core.MethodParameter
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice

@ControllerAdvice(assignableTypes = [ContentPartnerController::class])
class ContentPartnerControllerAdvice(private val projectionResolver: ProjectionResolver) :
    AbstractMappingJacksonResponseBodyAdvice() {
    override fun beforeBodyWriteInternal(
        bodyContainer: MappingJacksonValue,
        contentType: MediaType,
        returnType: MethodParameter,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ) {
        if (request is ServletServerHttpRequest && request.servletRequest.method == "GET") {
            bodyContainer.serializationView = (projectionResolver.resolveProjection())
        }
    }
}
