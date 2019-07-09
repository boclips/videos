package com.boclips.videos.service.application.contentPartner

import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class RequestBulkVideoSearchUpdateByContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var requestVideoSearchUpdateByContentPartner: RequestBulkVideoSearchUpdateByContentPartner

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `publishes an inclusion event for all delivery methods when delivery methods are not specified`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), emptySet()
        )

        val downloadRequestedMessage = messageCollector.forChannel(topics.videosInclusionInDownloadRequested()).poll()
        val downloadRequestedEvent =
            objectMapper.readValue(
                downloadRequestedMessage.payload.toString(),
                VideosInclusionInDownloadRequested::class.java
            )

        val streamRequestedMessage = messageCollector.forChannel(topics.videosInclusionInStreamRequested()).poll()
        val streamRequestedEvent =
            objectMapper.readValue(
                streamRequestedMessage.payload.toString(),
                VideosInclusionInStreamRequested::class.java
            )


        assertThat(downloadRequestedEvent.videoIds).contains(videoId.value)
        assertThat(streamRequestedEvent.videoIds).contains(videoId.value)
    }

    @Test
    fun `publishes an exclusion event for all delivery methods when hiding all delivery methods`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), DeliveryMethod.ALL
        )

        val downloadRequestedMessage = messageCollector.forChannel(topics.videosExclusionFromDownloadRequested()).poll()
        val downloadRequestedEvent =
            objectMapper.readValue(
                downloadRequestedMessage.payload.toString(),
                VideosExclusionFromDownloadRequested::class.java
            )

        val streamRequestedMessage = messageCollector.forChannel(topics.videosExclusionFromStreamRequested()).poll()
        val streamRequestedEvent =
            objectMapper.readValue(
                streamRequestedMessage.payload.toString(),
                VideosExclusionFromStreamRequested::class.java
            )


        assertThat(downloadRequestedEvent.videoIds).contains(videoId.value)
        assertThat(streamRequestedEvent.videoIds).contains(videoId.value)
    }

    @Test
    fun `publishes an exclusion event for single delivery methods when hiding just one`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), setOf(DeliveryMethod.DOWNLOAD)
        )

        val downloadExclusionMessage = messageCollector.forChannel(topics.videosExclusionFromDownloadRequested()).poll()
        val downloadExclusionEvent =
            objectMapper.readValue(
                downloadExclusionMessage.payload.toString(),
                VideosExclusionFromDownloadRequested::class.java
            )

        val streamInclusionMessage = messageCollector.forChannel(topics.videosInclusionInStreamRequested()).poll()
        val streamInclusionEvent =
            objectMapper.readValue(
                streamInclusionMessage.payload.toString(),
                VideosInclusionInStreamRequested::class.java
            )


        assertThat(downloadExclusionEvent.videoIds).contains(videoId.value)
        assertThat(streamInclusionEvent.videoIds).contains(videoId.value)
    }

    @Test
    fun `throws if content partner is fictitious`() {
        assertThrows<ContentPartnerNotFoundException> {
            requestVideoSearchUpdateByContentPartner.invoke(
                ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), DeliveryMethod.ALL
            )
        }
    }

    @Test
    fun `publishes multiple events if videos are batched`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), setOf(DeliveryMethod.STREAM)
        )

        assertThat(messageCollector.forChannel(topics.videosExclusionFromStreamRequested()).size).isEqualTo(2)
    }
}
