package com.boclips.videos.service.client

import com.boclips.videos.service.client.exceptions.VideoNotFoundException
import com.boclips.videos.service.client.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.net.URI
import java.time.Duration


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
internal abstract class VideoServiceClientContractTest : AbstractSpringIntegrationTest() {

    abstract fun getClient(): VideoServiceClient

    @Test
    fun `get VideoId for raw identifier`() {
        val rawId = getClient().create(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123")).uri.toString().split('/').last()

        val id = getClient().rawIdToVideoId(rawId)

        assertThat(id.uri.toString()).matches("https?://.*/videos/$rawId")
    }

    @Test
    fun `create a video gives a unique id`() {
        val id1 = getClient().create(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123"))
        val id2 = getClient().create(TestFactories.createCreateVideoRequest(playbackId = "ref-id-123"))

        assertThat(id1.uri.toString()).contains("/videos/")
        assertThat(id1.uri.toString()).isNotEqualTo(id2.uri.toString())
    }

    @Test
    fun `lookup video by content partner id`() {
        getClient().create(TestFactories.createCreateVideoRequest(contentProviderId = "ted", contentProviderVideoId = "123", playbackId = "ref-id-123"))

        assertThat(getClient().existsByContentPartnerInfo("ted", "123")).isTrue()
        assertThat(getClient().existsByContentPartnerInfo("ted", "124")).isFalse()
    }

    @Test
    fun `tag videos with subjects`() {
        val id = getClient().create(TestFactories.createCreateVideoRequest(contentProviderId = "ted", contentProviderVideoId = "123", playbackId = "ref-id-123"))

        getClient().setSubjects(id, setOf("maths", "physics"))

        val video = getClient().get(id)
        assertThat(video.subjects).containsExactly("maths", "physics")
    }

    @Test
    fun `tag videos with subjects throws when video doesn't exist`() {
        val id = getClient().create(TestFactories.createCreateVideoRequest(contentProviderId = "ted", contentProviderVideoId = "123", playbackId = "ref-id-123"))

        val nonExistingId = VideoId(URI(id.uri.toString() + "111"))

        assertThrows<VideoNotFoundException> {
            getClient().setSubjects(nonExistingId, setOf("maths"))
        }
    }
}

internal class FakeVideoServiceClientContractTest : VideoServiceClientContractTest() {
    val fakeClient = VideoServiceClient.getFakeClient()
    override fun getClient() = fakeClient

}

internal class ApiVideoServiceClientContractTest : VideoServiceClientContractTest() {
    val realClient = VideoServiceClient.getUnauthorisedApiClient("http://localhost:9876")

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-123", referenceId = "ref-id-123", duration = Duration.ofMinutes(1)))
    }

    override fun getClient() = realClient
}
