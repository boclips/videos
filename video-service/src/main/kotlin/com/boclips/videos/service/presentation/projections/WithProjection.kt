package com.boclips.videos.service.presentation.projections

import org.springframework.hateoas.ResourceSupport
import org.springframework.http.converter.json.MappingJacksonValue

class WithProjection(private val projectionResolver: ProjectionResolver) {

    operator fun invoke(payload: ResourceSupport) =
        MappingJacksonValue(payload)
            .apply {
                serializationView = projectionResolver.resolveProjection()
            }
}
