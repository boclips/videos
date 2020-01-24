package com.boclips.videos.api.response.discipline

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class DisciplinesResource(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _embedded: DisciplinesWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class DisciplinesWrapperResource(
    val disciplines: List<DisciplineResource>
)
