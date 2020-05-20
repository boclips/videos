package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.channel.CustomIngest
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.infrastructure.contentpartner.converters.IngestDetailsDocumentConverter
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
        val ingestDetails = MrssFeedIngest(
            urls = listOf("http://feed.me")
        )
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(
            MrssFeedIngest(
                urls = listOf("http://feed.me")
            )
        )
    }

    @Test
    fun `convert youtube ingest details to document and back`() {
        val ingestDetails = YoutubeScrapeIngest(
            playlistIds = listOf("http://you.tube")
        )
            .let(IngestDetailsDocumentConverter::toIngestDetailsDocument)
            .let(IngestDetailsDocumentConverter::toIngestDetails)

        assertThat(ingestDetails).isEqualTo(
            YoutubeScrapeIngest(
                playlistIds = listOf("http://you.tube")
            )
        )
    }

    @Test
    fun `handle legacy youtube documents with urls populated`() {
        val document =
            IngestDetailsDocument(
                type = "YOUTUBE",
                urls = listOf("playlist-id"),
                playlistIds = null
            )

        val ingestDetails = IngestDetailsDocumentConverter.toIngestDetails(document)

        assertThat(ingestDetails).isEqualTo(
            YoutubeScrapeIngest(
                playlistIds = listOf("playlist-id")
            )
        )
    }
}
