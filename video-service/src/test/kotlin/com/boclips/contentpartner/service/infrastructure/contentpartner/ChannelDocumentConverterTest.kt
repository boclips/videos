package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.infrastructure.channel.converters.ChannelDocumentConverter
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannelDocument
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL
import java.util.*

class ChannelDocumentConverterTest {

    @Test
    fun `round-trips a channel conversion via document`() {
        val original = Channel(
            id = ChannelId(
                value = ObjectId().toHexString()
            ),
            name = "The grandest content partner there ever lived",
            legalRestriction = ChannelFactory.createLegalRestrictions(),
            distributionMethods = setOf(DistributionMethod.DOWNLOAD),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "This is a description",
            contentCategories = listOf(ContentCategory.ANIMATION),
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            contentTypes = listOf(ContentType.INSTRUCTIONAL),
            ingest = MrssFeedIngest(
                urls = listOf("https://mrss.feed")
            ),
            marketingInformation = MarketingInformation(
                oneLineDescription = "1l",
                status = ChannelStatus.NEEDS_INTRODUCTION,
                logos = listOf(URL("http://www.server.com/1.png")),
                showreel = URL("http://www.server.com/2.png"),
                sampleVideos = listOf(URL("http://www.server.com/3.png"))
            ),
            pedagogyInformation = PedagogyInformation(
                bestForTags = listOf("123", "345"),
                subjects = listOf("subject 1", "subjects 2"),
                ageRangeBuckets = AgeRangeBuckets(
                    listOf(ChannelFactory.createAgeRange())
                )
            ),
            contract = ContentPartnerContractFactory.sample(),
            taxonomy = Taxonomy.ChannelLevelTagging(
                categories = setOf(
                    CategoryWithAncestors(
                        codeValue = CategoryCode("AB"),
                        description = "A lovely lovely description",
                        ancestors = setOf(CategoryCode("A"))
                    )
                )
            ),
            visibility = ChannelVisibility.PRIVATE
        )

        val document = ChannelDocumentConverter.toChannelDocument(original)
        val convertedAsset = ChannelDocumentConverter.toChannel(document)

        assertThat(convertedAsset).isEqualTo(original)
    }

    @Test
    fun `ingest defaults to manual when not specified in the document`() {
        val document = createChannelDocument(ingest = null)
        val contentPartner = ChannelDocumentConverter.toChannel(document)

        assertThat(contentPartner.ingest).isEqualTo(ManualIngest)
    }

    @Nested
    inner class DistributionMethods {
        @Test
        fun `the content partner is available on all distribution methods by default`() {
            val document = createChannelDocument(distributionMethods = null)

            val convertedAsset = ChannelDocumentConverter.toChannel(document)
            assertThat(convertedAsset.distributionMethods).isEqualTo(DistributionMethod.ALL)
        }

        @Test
        fun `the content partner is available on all distribution methods`() {
            val contentPartner = ChannelFactory.createChannel(
                distributionMethods = emptySet()
            )

            val contentPartnerDocument = ChannelDocumentConverter.toChannelDocument(contentPartner)
            val convertedContentPartner = ChannelDocumentConverter.toChannel(contentPartnerDocument)

            assertThat(convertedContentPartner.id.value).isEqualTo(contentPartner.id.value)
        }

        @Test
        fun `the content partner is not available at all`() {
            val contentPartner = ChannelFactory.createChannel(
                distributionMethods = DistributionMethod.ALL
            )

            val contentPartnerDocument = ChannelDocumentConverter.toChannelDocument(contentPartner)
            val convertedContentPartner = ChannelDocumentConverter.toChannel(contentPartnerDocument)

            assertThat(convertedContentPartner.id.value).isEqualTo(contentPartner.id.value)
        }

        @Test
        fun `the content partner is only available for download`() {
            val contentPartner = ChannelFactory.createChannel(
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )

            val contentPartnerDocument = ChannelDocumentConverter.toChannelDocument(contentPartner)
            val convertedContentPartner = ChannelDocumentConverter.toChannel(contentPartnerDocument)

            assertThat(convertedContentPartner.id.value).isEqualTo(contentPartner.id.value)
        }

        @Test
        fun `the content partner is only available for stream`() {
            val contentPartner = ChannelFactory.createChannel(
                distributionMethods = setOf(DistributionMethod.STREAM)
            )

            val contentPartnerDocument = ChannelDocumentConverter.toChannelDocument(contentPartner)
            val convertedContentPartner = ChannelDocumentConverter.toChannel(contentPartnerDocument)

            assertThat(convertedContentPartner.id.value).isEqualTo(contentPartner.id.value)
        }
    }

    @Nested
    inner class Types {
        @Test
        fun `invalid types are ignored`() {
            val contentPartnerDocument =
                createChannelDocument(contentTypes = listOf("NEWS", "INSTRUCTIONAL", "STOCK", "WHODIS?"))

            val convertedContentPartner = ChannelDocumentConverter.toChannel(contentPartnerDocument)

            assertThat(convertedContentPartner.contentTypes).containsExactly(
                ContentType.NEWS,
                ContentType.INSTRUCTIONAL,
                ContentType.STOCK
            )
        }
    }
}
