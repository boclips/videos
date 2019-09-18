package com.boclips.videos.service.client.testsupport

import com.boclips.videos.service.client.CollectionId
import com.boclips.videos.service.client.CreateContentPartnerRequest
import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.PlaybackProvider
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId
import java.net.URI
import java.time.LocalDate

object TestFactories {
    fun createContentPartnerRequest(
        name: String = "TeD",
        accreditedToYtChannelId: String? = null
    ): CreateContentPartnerRequest {
        return CreateContentPartnerRequest.builder()
            .name(name)
            .accreditedToYtChannelId(accreditedToYtChannelId)
            .build()
    }

    fun createVideoId(): VideoId {
        val id = TestFactories.aValidId()
        return VideoId(URI.create("https://video-service.com/v1/videos/$id"))
    }

    fun createCollectionId(): CollectionId {
        val id = TestFactories.aValidId()
        return CollectionId(URI.create("https://video-service.com/v1/collections/$id"))
    }

    fun createCreateVideoRequest(
        contentProviderId: String = ObjectId().toHexString(),
        contentProviderVideoId: String = "ted-123",
        title: String = "video title",
        description: String = "video description",
        releasedOn: LocalDate = LocalDate.now(),
        legalRestrictions: String = "None",
        keywords: List<String> = listOf("k1", "k2"),
        contentType: VideoType = VideoType.INSTRUCTIONAL_CLIPS,
        playbackId: String = "kaltura-id-789",
        playbackProvider: PlaybackProvider = PlaybackProvider.KALTURA,
        subjects: Set<String> = emptySet()
    ): CreateVideoRequest {
        return CreateVideoRequest.builder()
            .providerId(contentProviderId)
            .providerVideoId(contentProviderVideoId)
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
