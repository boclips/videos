package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.VideoDocument
import java.time.Duration
import java.time.LocalDate

object SearchableVideoMetadataFactory {
    fun create(
        id: String,
        title: String = "",
        description: String = "",
        contentProvider: String = "",
        releaseDate: LocalDate = LocalDate.now(),
        keywords: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        durationSeconds: Long = 0,
        source: SourceType = SourceType.YOUTUBE,
        transcript: String? = null,
        ageRangeMin: Int? = 3,
        ageRangeMax: Int? = 11,
        type: VideoType = VideoType.INSTRUCTIONAL,
        subjects: Set<SubjectMetadata> = emptySet()
    ) = VideoMetadata(
        id = id,
        title = title,
        description = description,
        contentProvider = contentProvider,
        releaseDate = releaseDate,
        keywords = keywords,
        tags = tags,
        durationSeconds = durationSeconds,
        source = source,
        transcript = transcript,
        ageRangeMin = ageRangeMin,
        ageRangeMax = ageRangeMax,
        type = type,
        subjects = subjects
    )
}

object TestFactories {
    fun createSubjectMetadata(
        id: String = "subject-id",
        name: String = "subject-name"
    ) = SubjectMetadata(
        id = id,
        name = name
    )

    fun createVideoDocument(releaseDate: LocalDate): VideoDocument {
        return VideoDocument(
            id = "1",
            title = "title",
            description = "description",
            contentProvider = "contentProvider",
            keywords = listOf("keywords"),
            tags = listOf("tags"),
            releaseDate = releaseDate,
            durationSeconds = 10,
            source = "Boclips",
            transcript = null,
            ageRangeMin = 3,
            ageRangeMax = 11,
            subjectIds = setOf("boring-subject"),
            subjectNames = setOf("boring-names"),
            type = VideoType.INSTRUCTIONAL.name
        )
    }
}

object SearchableCollectionMetadataFactory {
    fun create(
        id: String,
        title: String = "",
        subjects: List<String> = emptyList()
    ) = CollectionMetadata(
        id = id,
        title = title,
        subjectIds = subjects,
        visibility = CollectionVisibility.PRIVATE,
        owner = "some-user-id",
        bookmarkedByUsers = setOf("some-user-id")
    )
}

object LegacyVideoMetadataFactory {
    fun create(
        id: String,
        title: String = "title",
        description: String = "description",
        keywords: List<String> = listOf("keyword"),
        duration: Duration = Duration.ofSeconds(10),
        contentPartnerName: String = "contentPartnerName",
        contentPartnerVideoId: String = "contentPartnerVideoId",
        videoType: String = "Instructional Clips",
        releaseDate: LocalDate = LocalDate.now()
    ) = LegacyVideoMetadata(
        id = id,
        title = title,
        description = description,
        keywords = keywords,
        duration = duration,
        contentPartnerName = contentPartnerName,
        contentPartnerVideoId = contentPartnerVideoId,
        videoTypeTitle = videoType,
        releaseDate = releaseDate
    )
}
