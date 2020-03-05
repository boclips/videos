package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
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
        subjects: Set<SubjectMetadata> = emptySet(),
        subjectsSetManually: Boolean? = null,
        promoted: Boolean? = null,
        meanRating: Double? = 5.0,
        isClassroom: Boolean = false
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
        subjects = SubjectsMetadata(items = subjects, setManually = subjectsSetManually),
        promoted = promoted,
        meanRating = meanRating,
        isClassroom = isClassroom
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
            ageRange = (3..11).toList(),
            subjectIds = setOf("boring-subject"),
            subjectNames = setOf("boring-names"),
            type = VideoType.INSTRUCTIONAL.name,
            promoted = null,
            meanRating = null,
            subjectsSetManually = null,
            isClassroom = null
        )
    }
}

object SearchableCollectionMetadataFactory {
    fun create(
        id: String,
        title: String = "",
        visibility: CollectionVisibility = CollectionVisibility.PUBLIC,
        subjects: List<String> = emptyList(),
        hasAttachments: Boolean = false,
        owner: String = "some-user-id",
        bookmarkedBy: Set<String> = setOf(owner),
        description: String = "some default description",
        hasLessonPlans: Boolean? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null
    ) = CollectionMetadata(
        id = id,
        title = title,
        subjectIds = subjects,
        visibility = visibility,
        owner = owner,
        bookmarkedByUsers = bookmarkedBy,
        hasAttachments = hasAttachments,
        description = description,
        hasLessonPlans = hasLessonPlans,
        ageRangeMin = ageRangeMin,
        ageRangeMax = ageRangeMax
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
