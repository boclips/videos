package com.boclips.videos.service.client

import com.boclips.videos.service.client.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.crypto.tls.ConnectionEnd.client
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.stream.Stream


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
abstract class VideoServiceClientContractTest : AbstractSpringIntegrationTest() {

    abstract fun getClient() : VideoServiceClient

    @Test
    fun `create a video`() {
        getClient().create(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123"))
    }

    @Test
    fun `lookup video by content partner id`() {
        getClient().create(TestFactories.createCreateVideoRequest(contentProviderId = "ted", contentProviderVideoId = "123", playbackId = "ref-id-123"))

        assertThat(getClient().existsByContentPartnerInfo("ted", "123")).isTrue()


        assertThat(getClient().existsByContentPartnerInfo("ted", "124")).isFalse()
    }

}

class FakeVideoServiceClientContractTest: VideoServiceClientContractTest() {
    val fakeClient = FakeClient()
    override fun getClient() = fakeClient

}

class ApiVideoServiceClientContractTest: VideoServiceClientContractTest() {
    val realClient = ApiClient("http://localhost:9876")

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-123", referenceId = "ref-id-123", duration = Duration.ofMinutes(1)))
    }

    override fun getClient() = realClient
}
