package com.boclips.search.service.testsupport

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
        keywords: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ) = VideoMetadata(
        id = id,
        title = title,
        description = description,
        contentProvider = contentProvider,
        keywords = keywords,
        tags = tags
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
