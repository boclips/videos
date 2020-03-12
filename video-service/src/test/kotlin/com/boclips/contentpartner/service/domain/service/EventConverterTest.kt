package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.videos.api.response.contentpartner.IngestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EventConverterTest {

    private val converter: EventConverter = EventConverter()

    @Test
    fun `convert manual ingest details`() {
        val payload = converter.toIngestDetailsPayload(ManualIngest)

        assertThat(payload.type).isEqualTo(IngestType.MANUAL.name)
        assertThat(payload.url).isNull()
    }

    @Test
    fun `convert custom ingest details`() {
        val payload = converter.toIngestDetailsPayload(CustomIngest)

        assertThat(payload.type).isEqualTo(IngestType.CUSTOM.name)
        assertThat(payload.url).isNull()
    }

    @Test
    fun `convert mrss ingest details`() {
        val payload = converter.toIngestDetailsPayload(MrssFeedIngest("http://feed.me"))

        assertThat(payload.type).isEqualTo(IngestType.MRSS.name)
        assertThat(payload.url).isEqualTo("http://feed.me")
    }

    @Test
    fun `convert youtube ingest details`() {
        val payload = converter.toIngestDetailsPayload(YoutubeScrapeIngest("http://yt.channel"))

        assertThat(payload.type).isEqualTo(IngestType.YOUTUBE.name)
        assertThat(payload.url).isEqualTo("http://yt.channel")
    }
}
