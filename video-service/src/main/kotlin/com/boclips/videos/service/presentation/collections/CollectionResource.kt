package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.videos.service.presentation.video.VideoResource
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.Instant

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String,
    val owner: String,
    val title: String,
    val videos: List<Resource<VideoResource>>,
    val updatedAt: Instant,
    val isPublic: Boolean,
    @get:JsonIgnore
    val isBookmarked: Boolean,
    val isMine: Boolean,
    val createdBy: String,
    val subjects: Set<Resource<SubjectResource>>,
    val ageRange: AgeRangeResource?
)

data class AgeRangeResource(
    val min: Int,
    val max: Int
) {

    fun getLabel() = "$min-$max"
}
