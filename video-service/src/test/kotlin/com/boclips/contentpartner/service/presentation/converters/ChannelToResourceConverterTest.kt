package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ContentCategory
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.Remittance
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.channel.ContentTypeResource
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
import com.boclips.videos.api.response.channel.TaxonomyCategoryResource
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder
import java.time.Period
import java.util.Currency
import java.util.Locale

class ChannelToResourceConverterTest {
    lateinit var channelToResourceConverter: ChannelToResourceConverter

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        channelToResourceConverter =
            ChannelToResourceConverter(
                channelLinkBuilder = ChannelLinkBuilder(
                    mock
                ),
                ingestDetailsToResourceConverter = IngestDetailsResourceConverter(),
                legalRestrictionsToResourceConverter = LegalRestrictionsToResourceConverter(
                    mock()
                )
            )
    }

    @Test
    fun `convert channel to resource`() {
        val channel = createChannel(
            legalRestriction = ChannelFactory.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "this is a description",
            contentCategories = listOf(ContentCategory.ANIMATION),
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK),
            contract = ContentPartnerContractFactory.sample(id = "id", contentPartnerName = "TED"),
            taxonomy = Taxonomy.ChannelLevelTagging(
                categories = setOf(
                    CategoryWithAncestors(codeValue = CategoryCode("ABC"), description = "A description"),
                    CategoryWithAncestors(codeValue = CategoryCode("BC"), description = "B description")
                )
            ),
        )

        val channelResource = channelToResourceConverter.convert(channel, Projection.details)

        assertThat(channelResource.id).isNotEmpty()
        assertThat(channelResource.name).isNotEmpty()
        assertThat(channelResource.legalRestriction).isNotNull
        assertThat(channelResource.legalRestriction?.text).isEqualTo("Forbidden in the EU")
        assertThat(channelResource.distributionMethods).isEqualTo(setOf(DistributionMethodResource.STREAM))
        assertThat(channelResource.currency).isEqualTo("GBP")
        assertThat(channelResource.description).isEqualTo("this is a description")
        assertThat(channelResource.contentCategories?.first()?.key).isEqualTo("ANIMATION")
        assertThat(channelResource.notes).isEqualTo("first note")
        assertThat(channelResource.language?.code).isEqualTo(Locale.forLanguageTag("spa"))
        assertThat(channelResource.language?.name).isEqualTo("Spanish")
        assertThat(channelResource.contentTypes).hasSize(2)
        assertThat(channelResource.contentTypes).containsExactlyInAnyOrder(
            ContentTypeResource.INSTRUCTIONAL,
            ContentTypeResource.STOCK
        )
        assertThat(channelResource.ingest).isEqualTo(
            IngestDetailsResource(
                type = IngestType.MRSS,
                urls = listOf("https://feed.mrss")
            )
        )
        assertThat(channelResource.contractId).isEqualTo("id")
        assertThat(channelResource.contractName).isEqualTo("TED")
        assertThat(channelResource.taxonomy?.categories).containsExactlyInAnyOrder(
            TaxonomyCategoryResource(codeValue = "ABC", "A description"),
            TaxonomyCategoryResource(codeValue = "BC", "B description")
        )
        assertThat(channelResource.taxonomy?.requiresVideoLevelTagging).isNull()
    }

    @Test
    fun `convert content partner to resource with only ID and name for 'list' projection`() {
        val contentPartner = createChannel(
            legalRestriction = ChannelFactory.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "this is a description",
            contentCategories = listOf(ContentCategory.ANIMATION),
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK),
            contract = ContentPartnerContractFactory.sample(id = "id", contentPartnerName = "TED")
        )

        val contentPartnerResource = channelToResourceConverter.convert(contentPartner, Projection.list)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.name).isNotEmpty()
        assertThat(contentPartnerResource.legalRestriction).isNull()
        assertThat(contentPartnerResource.distributionMethods).isNull()
        assertThat(contentPartnerResource.currency).isNull()
        assertThat(contentPartnerResource.description).isNull()
        assertThat(contentPartnerResource.contentCategories).isNull()
        assertThat(contentPartnerResource.notes).isNull()
        assertThat(contentPartnerResource.language).isNull()
        assertThat(contentPartnerResource.contentTypes).isNull()
        assertThat(contentPartnerResource.ingest).isNull()
        assertThat(contentPartnerResource.contractId).isNull()
        assertThat(contentPartnerResource.contractName).isNull()
    }

    @Test
    fun `a contract currency takes precedence over a content partner currency`() {
        val contentPartner = createChannel(
            legalRestriction = ChannelFactory.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "this is a description",
            contentCategories = listOf(ContentCategory.ANIMATION),
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK),
            contract = ContentPartnerContractFactory.sample(
                id = "id",
                contentPartnerName = "TED",
                remittanceCurrency = "USD"
            )
        )

        val contentPartnerResource = channelToResourceConverter.convert(contentPartner, Projection.details)

        assertThat(contentPartnerResource.currency).isEqualTo("USD")
    }

    @Test
    fun `can convert paged results`() {
        val resultsPage: ResultsPage<Channel> = ResultsPage(
            elements = emptyList(),
            pageInfo = PageInfo(
                hasMoreElements = true,
                totalElements = 10,
                pageRequest = PageRequest(
                    size = 3,
                    page = 1
                )
            )
        )

        val converted = channelToResourceConverter.convert(resultsPage = resultsPage, projection = null)

        assertThat(converted._embedded.channels).isEmpty()
        assertThat(converted.page!!.totalPages).isEqualTo(4)
        assertThat(converted.page!!.number).isEqualTo(1)
        assertThat(converted.page!!.size).isEqualTo(3)
        assertThat(converted.page!!.totalElements).isEqualTo(10)
    }
}
