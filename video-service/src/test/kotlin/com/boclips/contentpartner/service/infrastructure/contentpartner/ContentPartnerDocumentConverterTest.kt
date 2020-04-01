package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerStatus
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import com.boclips.contentpartner.service.infrastructure.contentpartner.converters.ContentPartnerDocumentConverter
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartnerDocument
import com.boclips.videos.api.common.Specified
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Period
import java.util.Currency
import java.util.Locale

internal class ContentPartnerDocumentConverterTest {

    @Test
    fun `round-trips a content partner conversion via document`() {
        val original = ContentPartner(
            contentPartnerId = ContentPartnerId(
                value = ObjectId().toHexString()
            ),
            name = "The grandest content partner there ever lived",
            ageRangeBuckets = AgeRangeBuckets(
                listOf(ContentPartnerFactory.createAgeRange())
            ),
            credit = Credit.PartnerCredit,
            legalRestriction = ContentPartnerFactory.createLegalRestrictions(),
            distributionMethods = setOf(DistributionMethod.DOWNLOAD),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "This is a description",
            contentCategories = listOf("category_key"),
            hubspotId = "123456789d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            contentTypes = listOf(ContentPartnerType.INSTRUCTIONAL),
            ingest = MrssFeedIngest(
                urls = listOf("https://mrss.feed")
            ),
            deliveryFrequency = Period.ofMonths(3),
            marketingInformation = ContentPartnerMarketingInformation(
                oneLineDescription = "1l",
                status = ContentPartnerStatus.NEEDS_INTRODUCTION,
                logos = listOf(URL("http://www.server.com/1.png")),
                showreel = Specified(URL("http://www.server.com/2.png")),
                sampleVideos = listOf(URL("http://www.server.com/3.png"))
            ),
            pedagogyInformation = PedagogyInformation(
                isTranscriptProvided = true,
                educationalResources = "this is an edu resource",
                curriculumAligned = "this is a curriculum",
                bestForTags = listOf("123", "345"),
                subjects = listOf("subject 1", "subjects 2"),
                ageRangeBuckets = AgeRangeBuckets(
                    listOf(ContentPartnerFactory.createAgeRange())
                )
            )
        )

        val document = ContentPartnerDocumentConverter.toContentPartnerDocument(original)
        val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `ingest defaults to manual when not specified in the document`() {
        val document = createContentPartnerDocument(ingest = null)
        val contentPartner = ContentPartnerDocumentConverter.toContentPartner(document)

        assertThat(contentPartner.ingest).isEqualTo(ManualIngest)
    }

    @Nested
    inner class DistributionMethods {
        @Test
        fun `the content partner is available on all distribution methods by default`() {
            val document = createContentPartnerDocument(distributionMethods = null)

            val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
            assertThat(convertedAsset.distributionMethods).isEqualTo(DistributionMethod.ALL)
        }

        @Test
        fun `the content partner from youtube is not available for download by default`() {
            val document = createContentPartnerDocument(
                distributionMethods = null,
                youtubeChannelId = "Awesome channel"
            )

            val convertedAsset = ContentPartnerDocumentConverter.toContentPartner(document)
            assertThat(convertedAsset.distributionMethods).containsOnly(DistributionMethod.STREAM)
        }

        @Test
        fun `the content partner is available on all distribution methods`() {
            val contentPartner = ContentPartnerFactory.createContentPartner(
                distributionMethods = emptySet()
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner.contentPartnerId.value).isEqualTo(contentPartner.contentPartnerId.value)
        }

        @Test
        fun `the content partner is not available at all`() {
            val contentPartner = ContentPartnerFactory.createContentPartner(
                distributionMethods = DistributionMethod.ALL
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner.contentPartnerId.value).isEqualTo(contentPartner.contentPartnerId.value)
        }

        @Test
        fun `the content partner is only available for download`() {
            val contentPartner = ContentPartnerFactory.createContentPartner(
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner.contentPartnerId.value).isEqualTo(contentPartner.contentPartnerId.value)
        }

        @Test
        fun `the content partner is only available for stream`() {
            val contentPartner = ContentPartnerFactory.createContentPartner(
                distributionMethods = setOf(DistributionMethod.STREAM)
            )

            val contentPartnerDocument = ContentPartnerDocumentConverter.toContentPartnerDocument(contentPartner)
            val convertedContentPartner = ContentPartnerDocumentConverter.toContentPartner(contentPartnerDocument)

            assertThat(convertedContentPartner.contentPartnerId.value).isEqualTo(contentPartner.contentPartnerId.value)
        }
    }
}
