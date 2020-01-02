package com.boclips.videos.api.response.collection

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.VideoResource
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.ZonedDateTime

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String? = null,
    val owner: String? = null,
    val title: String? = null,
    val videos: List<Resource<VideoResource>>,
    val updatedAt: ZonedDateTime? = null,
    val public: Boolean? = null,
    @get:JsonIgnore
    val bookmarked: Boolean? = null,
    val mine: Boolean? = null,
    val createdBy: String? = null,
    val subjects: Set<Resource<SubjectResource>>,
    val ageRange: AgeRangeResource?,
    val description: String? = null,
    val attachments: Set<Resource<AttachmentResource>>?
)
