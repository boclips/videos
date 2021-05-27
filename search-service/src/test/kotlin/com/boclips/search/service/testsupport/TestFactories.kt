package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.channels.model.IngestType
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
import com.boclips.search.service.domain.videos.model.VideoCategoryCodes
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.VideoDocument
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

object SearchableVideoMetadataFactory {
    fun create(
        id: String,
        title: String = "",
        description: String = "",
        contentProvider: String = "",
        contentPartnerId: String = "",
        releaseDate: LocalDate = LocalDate.now(),
        keywords: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        durationSeconds: Long = 0,
        source: SourceType = SourceType.YOUTUBE,
        transcript: String? = null,
        ageRangeMin: Int? = 3,
        ageRangeMax: Int? = 11,
        subjects: Set<SubjectMetadata> = emptySet(),
        subjectsSetManually: Boolean? = null,
        promoted: Boolean? = null,
        meanRating: Double? = 5.0,
        eligibleForStream: Boolean = true,
        eligibleForDownload: Boolean = true,
        attachmentTypes: Set<String> = emptySet(),
        deactivated: Boolean = false,
        types: List<VideoType> = listOf(VideoType.INSTRUCTIONAL),
        ingestedAt: ZonedDateTime = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
        isVoiced: Boolean? = null,
        language: Locale? = null,
        prices: Map<String, BigDecimal>? = null,
        categoryCodes: List<String>? = emptyList()
    ) = VideoMetadata(
        id = id,
        title = title,
        rawTitle = title,
        description = description,
        contentProvider = contentProvider,
        contentPartnerId = contentPartnerId,
        releaseDate = releaseDate,
        keywords = keywords,
        tags = tags,
        durationSeconds = durationSeconds,
        source = source,
        transcript = transcript,
        ageRangeMin = ageRangeMin,
        ageRangeMax = ageRangeMax,
        subjects = SubjectsMetadata(items = subjects, setManually = subjectsSetManually),
        promoted = promoted,
        meanRating = meanRating,
        eligibleForStream = eligibleForStream,
        eligibleForDownload = eligibleForDownload,
        attachmentTypes = attachmentTypes,
        deactivated = deactivated,
        types = types,
        ingestedAt = ingestedAt,
        isVoiced = isVoiced,
        language = language,
        prices = prices,
        categoryCodes = categoryCodes?.let { VideoCategoryCodes(it) }
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

    fun createVideoDocument(releaseDate: LocalDate, ingestDate: ZonedDateTime): VideoDocument {
        return VideoDocument(
            id = "1",
            title = "title",
            rawTitle = "title",
            description = "description",
            contentProvider = "contentProvider",
            contentPartnerId = "cp-123456",
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
            promoted = null,
            meanRating = null,
            subjectsSetManually = null,
            eligibleForDownload = true,
            eligibleForStream = true,
            attachmentTypes = emptySet(),
            deactivated = false,
            types = listOf("NEWS"),
            ingestedAt = ingestDate,
            isVoiced = null,
            language = null,
            prices = null,
            categoryCodes = emptyList()
        )
    }
}

object SearchableCollectionMetadataFactory {
    fun create(
        id: String = "id1",
        title: String = "",
        subjects: List<String> = emptyList(),
        hasAttachments: Boolean = false,
        owner: String = "some-user-id",
        bookmarkedBy: Set<String> = setOf(owner),
        description: String = "some default description",
        hasLessonPlans: Boolean? = null,
        searchable: Boolean? = null,
        promoted: Boolean? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        lastModified: ZonedDateTime = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
        attachmentTypes: Set<String> = emptySet(),
        default: Boolean = false
    ) = CollectionMetadata(
        id = id,
        title = title,
        subjectIds = subjects,
        discoverable = searchable,
        promoted = promoted,
        owner = owner,
        bookmarkedByUsers = bookmarkedBy,
        hasAttachments = hasAttachments,
        description = description,
        hasLessonPlans = hasLessonPlans,
        ageRangeMin = ageRangeMin,
        ageRangeMax = ageRangeMax,
        lastModified = lastModified,
        attachmentTypes = attachmentTypes,
        default = default
    )
}

object SearchableChannelMetadataFactory {
    fun create(
        id: String = "id1",
        name: String = "",
        eligibleForStream: Boolean = false,
        contentTypes: List<ContentType> = emptyList(),
        ingestType: IngestType = IngestType.CUSTOM,
        taxonomy: Taxonomy = Taxonomy(videoLevelTagging = true)
    ) = ChannelMetadata(
        id = id,
        name = name,
        eligibleForStream = eligibleForStream,
        contentTypes = contentTypes,
        ingestType = ingestType,
        taxonomy = taxonomy
    )
}

object SearchableSubjectMetadataFactory {
    fun create(
        id: String = "id1",
        name: String = "",
    ) = SubjectMetadata(
        id = id,
        name = name
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
