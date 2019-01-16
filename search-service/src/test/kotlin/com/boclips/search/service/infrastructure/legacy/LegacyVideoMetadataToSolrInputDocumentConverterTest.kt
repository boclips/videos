package com.boclips.search.service.infrastructure.legacy

import com.boclips.search.service.testsupport.LegacyVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class LegacyVideoMetadataToSolrInputDocumentConverterTest {

    @Test
    fun `title is mapped to the title field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", title = "sexy teenagers")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("title").value).isEqualTo("sexy teenagers")
    }

    @Test
    fun `description is mapped to the description field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", description = "children playing with a dog")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("description").value).isEqualTo("children playing with a dog")
    }

    @Test
    fun `joined keywords are mapped to the keywords field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", keywords = listOf("k1", "k2"))
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("keywords").value).isEqualTo("k1, k2")
    }

    @Test
    fun `duration is mapped to the duration field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", duration = Duration.ofSeconds(100))
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("duration").value).isEqualTo("00:01:40")
    }

    @Test
    fun `duration in seconds is mapped to the durationsecs field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", duration = Duration.ofSeconds(950))
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("durationsecs").value).isEqualTo(950)
    }

    @Test
    fun `releaseDate in seconds is mapped to the clip_date field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", releaseDate = LocalDate.of(2018, 12, 19))
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("clip_date").value).isEqualTo("2018-12-19T00:00:00Z")
    }

    @Test
    fun `contentPartnerName is mapped to the source field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", contentPartnerName = "Ted Talks")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("source").value).isEqualTo("Ted Talks")
    }

    @Test
    fun `contentPartnerVideoId is mapped to the unique_id field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", contentPartnerVideoId = "123")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("unique_id").value).isEqualTo("123")
    }

    @Test
    fun `contentPartnerName and contentPartnerVideoId are mapped to the namespace field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", contentPartnerName = "Ted-Ed", contentPartnerVideoId = "abc")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("namespace").value).isEqualTo("Ted-Ed:abc")
    }

    @Test
    fun `videoType is mapped to the typename field`() {
        val video = LegacyVideoMetadataFactory.create(id = "1", videoType = "TED Talks")
        assertThat(LegacyVideoMetadataToSolrInputDocumentConverter.convert(video).getField("typename").value).isEqualTo("TED Talks")
    }
}