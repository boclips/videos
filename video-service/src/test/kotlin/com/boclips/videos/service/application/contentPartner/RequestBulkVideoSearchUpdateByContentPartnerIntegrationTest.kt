package com.boclips.videos.service.application.contentPartner

import com.boclips.eventbus.events.video.VideosExclusionFromDownloadRequested
import com.boclips.eventbus.events.video.VideosExclusionFromStreamRequested
import com.boclips.eventbus.events.video.VideosInclusionInDownloadRequested
import com.boclips.eventbus.events.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.video.DistributionMethod
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
            contentPartnerId = ContentPartnerId(value = "deadb33f1225df4825e8b8f6"),
            distributionMethods = DistributionMethod.ALL
        )

        val inclusionVideoDownloadEvent = fakeEventBus.getEventOfType(VideosInclusionInDownloadRequested::class.java)
        assertThat(inclusionVideoDownloadEvent.videoIds).containsExactly(videoId.value)

        val inclusionVideoStreamEvent = fakeEventBus.getEventOfType(VideosInclusionInStreamRequested::class.java)
        assertThat(inclusionVideoStreamEvent.videoIds).containsExactly(videoId.value)
    }

    @Test
    fun `publishes an exclusion event for all delivery methods when hiding all delivery methods`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), DistributionMethod.ALL
        )

        val exclusionFromDownloadEvent = fakeEventBus.getEventOfType(VideosInclusionInDownloadRequested::class.java)
        assertThat(exclusionFromDownloadEvent.videoIds).containsExactly(videoId.value)

        val exclusionFromStreamEvent = fakeEventBus.getEventOfType(VideosInclusionInStreamRequested::class.java)
        assertThat(exclusionFromStreamEvent.videoIds).containsExactly(videoId.value)
    }

    @Test
    fun `publishes an exclusion event for single delivery methods when hiding just one`() {
        val videoId = saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), setOf(DistributionMethod.DOWNLOAD)
        )

        val exclusionFromDownloadEvent = fakeEventBus.getEventOfType(VideosInclusionInDownloadRequested::class.java)
        assertThat(exclusionFromDownloadEvent.videoIds).containsExactly(videoId.value)

        val inclusionVideoStreamEvent = fakeEventBus.getEventOfType(VideosExclusionFromStreamRequested::class.java)
        assertThat(inclusionVideoStreamEvent.videoIds).containsExactly(videoId.value)
    }

    @Test
    fun `throws if content partner is fictitious`() {
        assertThrows<ContentPartnerNotFoundException> {
            requestVideoSearchUpdateByContentPartner.invoke(
                ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), DistributionMethod.ALL
            )
        }
    }

    @Test
    fun `publishes multiple events if videos are batched`() {
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")
        saveVideo(contentProviderId = "deadb33f1225df4825e8b8f6")

        requestVideoSearchUpdateByContentPartner.invoke(
            ContentPartnerId(value = "deadb33f1225df4825e8b8f6"), setOf(DistributionMethod.STREAM)
        )

        val eventCount = fakeEventBus.countEventsOfType(VideosExclusionFromDownloadRequested::class.java)
        assertThat(eventCount).isEqualTo(2)
    }
}
