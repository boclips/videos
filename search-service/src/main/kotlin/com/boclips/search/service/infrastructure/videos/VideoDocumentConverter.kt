package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.ESObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.search.SearchHit

object VideoDocumentConverter {

    fun fromSearchHit(searchHit: SearchHit): VideoDocument = ESObjectMapper.get()
        .readValue(searchHit.sourceAsString)

    fun fromVideo(video: VideoMetadata): VideoDocument {
        return VideoDocument(
            id = video.id,
            title = video.title,
            rawTitle = video.rawTitle,
            description = video.description,
            contentProvider = video.contentProvider,
            contentPartnerId = video.contentPartnerId,
            releaseDate = video.releaseDate,
            keywords = video.keywords,
            tags = video.tags,
            durationSeconds = video.durationSeconds,
            source = video.source.name,
            transcript = video.transcript,
            ageRangeMax = video.ageRangeMax,
            ageRangeMin = video.ageRangeMin,
            ageRange = AgeRange(video.ageRangeMin, video.ageRangeMax).toRange(),
            type = video.type.name,
            subjectIds = video.subjects.items.map { subject -> subject.id }.toSet(),
            subjectNames = video.subjects.items.map { subject -> subject.name }.toSet(),
            subjectsSetManually = video.subjects.setManually,
            promoted = video.promoted,
            meanRating = video.meanRating,
            eligibleForDownload = video.eligibleForDownload,
            eligibleForStream = video.eligibleForStream,
            attachmentTypes = video.attachmentTypes
        )
    }
}
