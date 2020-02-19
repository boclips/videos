package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.contentpartner.service.domain.model.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.MarketingInformation
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Currency
import java.util.Locale

internal class ContentPartnerDocumentConverterTest {

    @Test
    fun `round-trips a content partner conversion via document`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
            name = "The grandest content partner there ever lived",
            ageRangeBuckets = AgeRangeBuckets(listOf(TestFactories.createAgeRange())),
            credit = Credit.PartnerCredit,
            legalRestriction = TestFactories.createLegalRestrictions(),
            distributionMethods = setOf(DistributionMethod.DOWNLOAD),
            remittance = Remittance(Currency.getInstance("GBP")),
            description = "This is a description",
            contentCategories = listOf("category_key"),
            hubspotId = "123456789d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            contentTypes = listOf(ContentPartnerType.INSTRUCTIONAL),
            marketingInformation = MarketingInformation(
                oneLineDescription = "1l",
                status = ContentPartnerStatus.NeedsIntroduction
            ),
            isTranscriptProvided = true,
            educationalResources = "this is an edu resource",
            curriculumAligned = "this is a curriculum",
            bestForTags = listOf("123", "345"),
            subjects = listOf("subject 1", "subjects 2")
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
