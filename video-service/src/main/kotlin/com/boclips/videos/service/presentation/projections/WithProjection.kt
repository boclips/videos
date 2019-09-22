package com.boclips.videos.service.presentation.projections

import org.springframework.http.converter.json.MappingJacksonValue

class WithProjection(private val projectionResolver: ProjectionResolver) {
    operator fun invoke(payload: Any) =
        MappingJacksonValue(payload)
            .apply {
                serializationView = projectionResolver.resolveProjection()
            }
}