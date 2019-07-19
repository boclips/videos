package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CreateVideoRequestTest {

    @Test
    fun legalRestrictionsFieldIsNotRequired() {
        val video = buildVideo(legalRestrictions = null)

        assertThat(video.legalRestrictions).isNull()
    }

    private fun buildVideo(
        provider: String = "provider",
        providerId: String = "providerId",
        providerVideoId: String = "providerId video id",
        title: String = "title",
        description: String = "description",
        releasedOn: LocalDate = LocalDate.now(),
        legalRestrictions: String? = "legal restrictions",
        keywords: List<String> = emptyList(),
        contentType: VideoType = VideoType.NEWS,
        playbackId: String = "playback id",
        playbackProvider: PlaybackProvider = PlaybackProvider.KALTURA,
        subjects: Set<String> = emptySet()
    ): CreateVideoRequest {
        return CreateVideoRequest.builder()
            .provider(provider)
            .providerId(providerId)
            .providerVideoId(providerVideoId)
            .title(title)
            .description(description)
            .releasedOn(releasedOn)
            .legalRestrictions(legalRestrictions)
            .keywords(keywords)
            .videoType(contentType)
            .playbackId(playbackId)
            .playbackProvider(playbackProvider)
            .subjects(subjects)
            .build()
    }
}