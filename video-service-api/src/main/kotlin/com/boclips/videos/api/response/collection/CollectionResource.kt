package com.boclips.videos.api.response.collection

import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.service.presentation.ageRange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.ZonedDateTime

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String,
    val owner: String,
    val title: String,
    val videos: List<Resource<VideoResource>>,
    val updatedAt: ZonedDateTime,
    val public: Boolean,
    @get:JsonIgnore
    val bookmarked: Boolean,
    val mine: Boolean,
    val createdBy: String,
    val subjects: Set<Resource<SubjectResource>>,
    val ageRange: AgeRangeResource?,
    val description: String?,
    val attachments: Set<Resource<AttachmentResource>>?
)
