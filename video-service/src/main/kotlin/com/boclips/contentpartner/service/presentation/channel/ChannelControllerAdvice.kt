package com.boclips.contentpartner.service.presentation.channel

import com.boclips.videos.service.presentation.projections.ProjectionResolver
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice

@ControllerAdvice(assignableTypes = [ChannelController::class])
class ChannelControllerAdvice(private val projectionResolver: ProjectionResolver) :
    AbstractMappingJacksonResponseBodyAdvice() {
    override fun beforeBodyWriteInternal(
        bodyContainer: MappingJacksonValue,
        contentType: MediaType,
        returnType: MethodParameter,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ) {
        bodyContainer.serializationView = (projectionResolver.resolveProjection())
    }
}
