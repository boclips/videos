package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class CollectionDocumentConverter {
    fun convert(searchHit: SearchHit): CollectionDocument = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)

    fun convertToDocument(metadata: CollectionMetadata): CollectionDocument {
        return CollectionDocument(
            id = metadata.id,
            title = metadata.title,
            searchable = metadata.discoverable,
            subjects = metadata.subjectIds,
            hasAttachments = metadata.hasAttachments,
            owner = metadata.owner,
            bookmarkedBy = metadata.bookmarkedByUsers,
            description = metadata.description,
            hasLessonPlans = metadata.hasLessonPlans,
            promoted = metadata.promoted,
            ageRangeMin = metadata.ageRangeMin,
            ageRangeMax = metadata.ageRangeMax,
            ageRange = AgeRange(metadata.ageRangeMin, metadata.ageRangeMax).toRange(),
            updatedAt = metadata.updatedAt,
            attachmentTypes = metadata.attachmentTypes,
            default = metadata.default
        )
    }
}
