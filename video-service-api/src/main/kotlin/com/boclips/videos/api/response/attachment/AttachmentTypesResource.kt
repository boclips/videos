package com.boclips.videos.api.response.attachment

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

class AttachmentTypesResource(
    val _embedded: AttachmentTypesWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class AttachmentTypesWrapperResource(
    val attachmentTypes: List<AttachmentTypeResource>
)
