package com.boclips.videos.api.response.collection

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import java.time.ZonedDateTime

data class CollectionResource(
    val id: String? = null,
    val owner: String? = null,
    val title: String? = null,
    val videos: List<VideoResource>,
    val updatedAt: ZonedDateTime? = null,
    val public: Boolean? = null,
    @get:JsonIgnore
    val bookmarked: Boolean? = null,
    val mine: Boolean? = null,
    val createdBy: String? = null,
    val subjects: Set<SubjectResource>,
    val ageRange: AgeRangeResource?,
    val description: String? = null,
    val attachments: Set<AttachmentResource>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)
