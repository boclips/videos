package com.boclips.videos.service.client

import com.boclips.videos.service.client.testsupport.AbstractVideoServiceClientSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

abstract class VideoProjectionContractTest : AbstractVideoServiceClientSpringIntegrationTest() {
    abstract fun getClientWithPublicProjection(): VideoServiceClient

    abstract fun getClientWithInternalProjection(): VideoServiceClient

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(
            com.boclips.videos.service.testsupport.TestFactories.createMediaEntry(
                id = "entry-123",
                referenceId = "ref-id-123",
                duration = Duration.ofMinutes(1)
            )
        )
    }

    @Test
    fun `boclips internal projection has access to extra fields`() {
        val clientWithInternalProjection =
            getClientWithInternalProjection()

        val contentPartnerId =
            clientWithInternalProjection.createContentPartner(
                TestFactories.createContentPartnerRequest(name = "test-content-partner")
            )

        val playbackId = "ref-id-123"

        val id = clientWithInternalProjection.createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = playbackId,
                contentProviderId = contentPartnerId.value,
                contentType = VideoType.TED_ED
            )
        )

        val video = clientWithInternalProjection.get(id)
        assertThat(video.type).isEqualTo(VideoType.TED_ED)
    }

    @Test
    fun `public projection ignores extra fields`() {
        val clientWithPublicProjection =
            getClientWithPublicProjection()

        val contentPartnerId =
            clientWithPublicProjection.createContentPartner(
                TestFactories.createContentPartnerRequest(name = "test-content-partner")
            )

        val playbackId = "ref-id-123"
        val id = clientWithPublicProjection.createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = playbackId,
                contentProviderId = contentPartnerId.value,
                contentType = VideoType.TED_ED
            )
        )

        val video = clientWithPublicProjection.get(id)
        assertThat(video.type).isNull()
    }
}

class FakeClient : VideoProjectionContractTest() {
    override fun getClientWithPublicProjection(): VideoServiceClient {
        return VideoServiceClient.getFakeClient()
    }

    override fun getClientWithInternalProjection(): VideoServiceClient {
        val fake = VideoServiceClient.getFakeClient()
        fake.setUseInternalProjection(true)
        return fake
    }
}

class ApiClient : VideoProjectionContractTest() {
    override fun getClientWithPublicProjection(): VideoServiceClient {
        return VideoServiceClient.getUnauthorisedApiClient(videoServiceUrl)
    }

    override fun getClientWithInternalProjection(): VideoServiceClient {
        return VideoServiceClient.getUnauthorisedApiClient(videoServiceUrl, "internal@boclips.com")
    }
}

