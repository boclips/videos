package com.boclips.videos.service.application.contentPartner

import com.boclips.events.types.VideosExclusionFromSearchRequested
import com.boclips.events.types.VideosInclusionInSearchRequested
import com.boclips.videos.service.application.contentPartner.RequestBulkVideoSearchUpdateByContentPartner.RequestType
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
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
    fun `publishes an inclusion-in-search event for a content partner`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.INCLUDE
        )

        val message = messageCollector.forChannel(topics.videosInclusionInSearchRequested()).poll()
        val event =
            objectMapper.readValue(message.payload.toString(), VideosInclusionInSearchRequested::class.java)
        assertThat(event.videoIds).contains(videoId.value)
    }

    @Test
    fun `throws if content partner is fictitious`() {
        assertThrows<ContentPartnerNotFoundException> {
            requestVideoSearchUpdateByContentPartner.invoke(
                ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.INCLUDE
            )
        }
    }

    @Test
    fun `publishes an exclusion-from-search event for a content partner`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.EXCLUDE
        )

        val message = messageCollector.forChannel(topics.videosExclusionFromSearchRequested()).poll()
        val event =
            objectMapper.readValue(message.payload.toString(), VideosExclusionFromSearchRequested::class.java)
        assertThat(event.videoIds).contains(videoId.value)
    }

    @Test
    fun `publishes multiple events if videos are batched`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), RequestType.EXCLUDE
        )

        assertThat(messageCollector.forChannel(topics.videosExclusionFromSearchRequested()).size).isEqualTo(2)
    }
}