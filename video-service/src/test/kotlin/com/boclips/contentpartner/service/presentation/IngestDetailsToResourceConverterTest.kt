package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IngestDetailsToResourceConverterTest {

    private val converter = IngestDetailsToResourceConverter()

    @Test
    fun `convert manual ingest details`() {
        val ingest = ManualIngest

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestDetailsResource.MANUAL)
        assertThat(resource.url).isNull()
    }

    @Test
    fun `convert custom ingest details`() {
        val ingest = CustomIngest

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestDetailsResource.CUSTOM)
        assertThat(resource.url).isNull()
    }

    @Test
    fun `convert mrss feed ingest details`() {
        val ingest = MrssFeedIngest(url = "http://mrss.feed")

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestDetailsResource.MRSS)
        assertThat(resource.url).isEqualTo("http://mrss.feed")
    }

    @Test
    fun `convert youtube scrape ingest details`() {
        val ingest = YoutubeScrapeIngest(url = "http://yt.scrape")

        val resource = converter.convert(ingest)

        assertThat(resource.type).isEqualTo(IngestDetailsResource.YOUTUBE)
        assertThat(resource.url).isEqualTo("http://yt.scrape")
    }
}
