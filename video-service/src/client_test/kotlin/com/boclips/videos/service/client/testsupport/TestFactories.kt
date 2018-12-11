package com.boclips.videos.service.client.testsupport

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.PlaybackProvider
import com.boclips.videos.service.domain.model.asset.VideoType
import java.time.Duration
import java.time.LocalDate

object TestFactories {

    fun createCreateVideoRequest(
            contentProviderId: String = "ted",
            contentProviderVideoId: String = "ted-123",
            title: String = "video title",
            description: String = "video description",
            releasedOn: LocalDate = LocalDate.now(),
            duration: Duration = Duration.ofSeconds(10),
            legalRestrictions: String = "None",
            keywords: List<String> = listOf("k1", "k2"),
            contentType: String = VideoType.INSTRUCTIONAL_CLIPS.name,
            playbackId: String = "kaltura-id-789",
            playbackProvider: PlaybackProvider = PlaybackProvider.KALTURA
    ): CreateVideoRequest {
        return CreateVideoRequest(
                provider = contentProviderId,
                providerVideoId = contentProviderVideoId,
                title = title,
                description = description,
                releasedOn = releasedOn,
                duration = duration,
                legalRestrictions = legalRestrictions,
                keywords = keywords,
                contentType = contentType,
                playbackId = playbackId,
                playbackProvider = playbackProvider
        )
    }

}
