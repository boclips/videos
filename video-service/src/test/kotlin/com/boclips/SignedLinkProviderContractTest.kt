package com.boclips

import com.boclips.contentpartner.service.config.properties.GcsProperties
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.GcsSignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.TestSignedLinkProvider
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.util.stream.Stream

class SignedLinkProviderContractTest {
    @ParameterizedTest
    @ArgumentsSource(SignedLinkProviderArgumentProvider::class)
    fun `generates a link`(provider: SignedLinkProvider) {
        assertThat(provider.getLink()).isNotNull()
    }
}

class SignedLinkProviderArgumentProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val testSignedLinkProvider = TestSignedLinkProvider()
            .also { it.setLink(URI("https://my.sample.com/link").toURL()) }
        val gcsSignedLinkProvider = GcsSignedLinkProvider(
            loadGcsProperties()
        )
        return Stream.of(
            testSignedLinkProvider,
            gcsSignedLinkProvider
        ).map { Arguments.of(it) }
    }
}

fun loadGcsProperties(): GcsProperties {
    val projectIdKey = "GCS_PROJECT_ID"
    val secretKey = "GCS_SECRET"
    val bucketKey = "GCS_BUCKET_NAME"
    val projectId = System.getenv(projectIdKey)
    val secret = System.getenv(secretKey)
    val bucketName = System.getenv(bucketKey)

    if (projectId != null && secret != null && bucketName != null) {
        return GcsProperties(
            projectId = projectId,
            secret = secret,
            bucketName = bucketName
        )
    }

    val mapper = ObjectMapper()

    val inputStream: InputStream =
        SignedLinkProviderContractTest::javaClass.javaClass.classLoader
            .getResourceAsStream("contract-test-setup.json")
            ?: throw FileNotFoundException("Run ./setup to generate contract-test-setup.json")

    val json = mapper.readValue(inputStream, Map::class.java)
    return GcsProperties(
        projectId = json[projectIdKey] as String,
        secret = json[secretKey] as String,
        bucketName = json[bucketKey] as String
    )
}