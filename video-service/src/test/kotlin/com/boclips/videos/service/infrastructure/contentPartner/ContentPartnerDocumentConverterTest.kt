package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.infrastructure.ContentPartnerDocumentConverter
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ContentPartnerDocumentConverterTest {

    @Test
    fun `round-trips a content partner conversion via document`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
            name = "The grandest content partner there ever lived",
            ageRange = AgeRange.bounded(5, 11),
            credit = Credit.PartnerCredit,
            legalRestrictions = TestFactories.createLegalRestrictions(),
            distributionMethods = setOf(DistributionMethod.DOWNLOAD)
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Nested
    inner class DistributionMethods {
        @Test
        fun `the content partner is available on all distribution methods by default`() {
            val document = TestFactories.createContentPartnerDocument(distributionMethods = null)

            val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
            assertThat(convertedAsset.distributionMethods).isEqualTo(DistributionMethod.ALL)
        }

        @Test
        fun `the content partner from youtube is not available for download by default`() {
            val document = TestFactories.createContentPartnerDocument(
                distributionMethods = null,
                youtubeChannelId = "Awesome channel"
            )

            val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
            assertThat(convertedAsset.distributionMethods).containsOnly(DistributionMethod.STREAM)
        }

        @Test
        fun `the content partner is available on all distribution methods`() {
            val contentPartner = TestFactories.createContentPartner(
                distributionMethods = emptySet()
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner).isEqualTo(contentPartner)
        }

        @Test
        fun `the content partner is not available at all`() {
            val contentPartner = TestFactories.createContentPartner(
                distributionMethods = DistributionMethod.ALL
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner).isEqualTo(contentPartner)
        }

        @Test
        fun `the content partner is only available for download`() {
            val contentPartner = TestFactories.createContentPartner(
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner).isEqualTo(contentPartner)
        }

        @Test
        fun `the content partner is only available for stream`() {
            val contentPartner = TestFactories.createContentPartner(
                distributionMethods = setOf(DistributionMethod.STREAM)
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner).isEqualTo(contentPartner)
        }
    }
}
