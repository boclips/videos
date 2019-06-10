package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

internal class ContentPartnerDocumentConverterTest {

    @Test
    fun `converts a content partner to to document to content partner`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
            name = "The grandest content partner there ever lived",
            ageRange = AgeRange.bounded(5, 11)
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `invalid hex string is treated as a youtube channel name`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId("not a hex string"),
            name = "The grandest content partner there ever lived",
            ageRange = AgeRange.bounded(5, 11)
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `validates if an id is from youtube`() {
        assertThat(ContentPartnerDocumentConverter.isIdFromYoutube(ContentPartnerId(ObjectId.get().toHexString()))).isFalse()

        assertThat(ContentPartnerDocumentConverter.isIdFromYoutube(ContentPartnerId("definitely a youtube id"))).isTrue()
    }
}