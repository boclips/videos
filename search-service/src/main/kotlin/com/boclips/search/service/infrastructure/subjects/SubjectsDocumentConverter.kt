package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

class SubjectsDocumentConverter {
    fun convert(searchHit: SearchHit): SubjectDocument = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)

    fun convertToDocument(metadata: SubjectMetadata): SubjectDocument {
        return SubjectDocument(
            id = metadata.id,
            name = metadata.name
        )
    }
}