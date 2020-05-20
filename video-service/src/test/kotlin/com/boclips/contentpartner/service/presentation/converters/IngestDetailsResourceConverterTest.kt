package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.CustomIngest
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IngestDetailsResourceConverterTest {

    private val converter =
        IngestDetailsResourceConverter()

    private val mrssIngest =
        MrssFeedIngest(urls = listOf("http://mrss.feed"))

    private val youtubeIngest =
        YoutubeScrapeIngest(
            playlistIds = listOf("playlistId")
        )

    @Test
    fun `convert manual ingest details`() {
        val ingest = ManualIngest

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestType.MANUAL)
        assertThat(resource.urls).isNull()
    }

    @Test
    fun `convert custom ingest details`() {
        val ingest = CustomIngest

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestType.CUSTOM)
        assertThat(resource.urls).isNull()
    }

    @Test
    fun `convert mrss feed ingest details`() {
        val resource = converter.convert(mrssIngest)

        assertThat(resource.type).isEqualTo(IngestType.MRSS)
        assertThat(resource.urls).containsExactly("http://mrss.feed")
    }

    @Test
    fun `convert youtube scrape ingest details`() {
        val resource = converter.convert(youtubeIngest)

        assertThat(resource.type).isEqualTo(IngestType.YOUTUBE)
        assertThat(resource.playlistIds).containsExactly("playlistId")
    }

    @Test
    fun `convert back from resource`() {
        assertThat(ManualIngest.let(converter::convert).let(converter::fromResource)).isEqualTo(
            ManualIngest
        )
        assertThat(CustomIngest.let(converter::convert).let(converter::fromResource)).isEqualTo(
            CustomIngest
        )
        assertThat(mrssIngest.let(converter::convert).let(converter::fromResource)).isEqualTo(mrssIngest)
        assertThat(youtubeIngest.let(converter::convert).let(converter::fromResource)).isEqualTo(youtubeIngest)
    }

    @Test
    fun `convert youtube resource to ingest details when playlist ids are sent via the urls field`() {
        val resource = IngestDetailsResource(
            type = IngestType.YOUTUBE,
            playlistIds = null,
            urls = listOf("playlistId")
        )

        val ingestDetails = converter.fromResource(resource)

        assertThat(ingestDetails).isEqualTo(youtubeIngest)
    }
}
