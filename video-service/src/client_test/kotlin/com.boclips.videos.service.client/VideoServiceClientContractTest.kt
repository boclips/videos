package com.boclips.videos.service.client

import com.boclips.videos.service.client.testsupport.TestFactories
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.boot.test.context.SpringBootTest
import java.util.stream.Stream


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class VideoServiceClientContractTest : AbstractSpringIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(ClientArgumentProvider::class)
    fun `create a video`(client: VideoServiceClient) {
        client.create(TestFactories.createCreateVideoRequest())
    }

    @ParameterizedTest
    @ArgumentsSource(ClientArgumentProvider::class)
    fun `lookup video by content partner id`(client: VideoServiceClient) {
        client.create(TestFactories.createCreateVideoRequest(contentProviderId = "ted", contentProviderVideoId = "123"))

        assertThat(client.existsByContentPartnerInfo("ted", "123")).isTrue()
        assertThat(client.existsByContentPartnerInfo("ted", "124")).isFalse()
    }

}

class ClientArgumentProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val fakeClient = FakeClient()

//        val realClient = ApiClient("http://localhost:9876")

        return Stream.of(fakeClient)
                .map { client -> Arguments.of(client) }
    }
}
