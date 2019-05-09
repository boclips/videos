package com.boclips.search.service.testsupport

import com.boclips.search.service.domain.SourceType
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
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
        source: SourceType = SourceType.YOUTUBE
    ) = VideoMetadata(
        id = id,
        title = title,
        description = description,
        contentProvider = contentProvider,
        releaseDate = releaseDate,
        keywords = keywords,
        tags = tags,
        durationSeconds = durationSeconds,
        source = source
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
