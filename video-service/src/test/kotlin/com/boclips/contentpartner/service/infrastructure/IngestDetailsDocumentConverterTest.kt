package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IngestDetailsDocumentConverterTest {

    @Test
    fun `convert manual ingest details to document and back`() {
        val ingestDetails = ManualIngest
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(ManualIngest)
    }

    @Test
    fun `convert custom ingest details to document and back`() {
        val ingestDetails = CustomIngest
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(CustomIngest)
    }

    @Test
    fun `convert mrss ingest details to document and back`() {
        val ingestDetails = MrssFeedIngest(url = "http://feed.me")
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(MrssFeedIngest(url = "http://feed.me"))
    }

    @Test
    fun `convert youtube ingest details to document and back`() {
        val ingestDetails = YoutubeScrapeIngest(url = "http://you.tube")
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(YoutubeScrapeIngest(url = "http://you.tube"))
    }
}
