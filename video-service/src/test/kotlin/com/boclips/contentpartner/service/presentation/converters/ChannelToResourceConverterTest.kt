package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.ContentCategory
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.Remittance
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.channel.ContentTypeResource
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
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
    fun `convert content partner to resource`() {
        val contentPartner = createChannel(
            legalRestriction = ChannelFactory.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "this is a description",
            contentCategories = listOf(ContentCategory.ANIMATION),
            hubspotId = "12345678d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            deliveryFrequency = Period.ofMonths(3),
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK),
            contract = ContentPartnerContractFactory.sample(id = "id", contentPartnerName = "TED")
        )

        val contentPartnerResource = channelToResourceConverter.convert(contentPartner, Projection.details)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.name).isNotEmpty()
        assertThat(contentPartnerResource.legalRestriction).isNotNull
        assertThat(contentPartnerResource.legalRestriction?.text).isEqualTo("Forbidden in the EU")
        assertThat(contentPartnerResource.distributionMethods).isEqualTo(setOf(DistributionMethodResource.STREAM))
        assertThat(contentPartnerResource.currency).isEqualTo("GBP")
        assertThat(contentPartnerResource.description).isEqualTo("this is a description")
        assertThat(contentPartnerResource.contentCategories?.first()?.key).isEqualTo("ANIMATION")
        assertThat(contentPartnerResource.hubspotId).isEqualTo("12345678d")
        assertThat(contentPartnerResource.awards).isEqualTo("first award")
        assertThat(contentPartnerResource.notes).isEqualTo("first note")
        assertThat(contentPartnerResource.language?.code).isEqualTo(Locale.forLanguageTag("spa"))
        assertThat(contentPartnerResource.language?.name).isEqualTo("Spanish")
        assertThat(contentPartnerResource.contentTypes).hasSize(2)
        assertThat(contentPartnerResource.contentTypes).containsExactlyInAnyOrder(
            ContentTypeResource.INSTRUCTIONAL,
            ContentTypeResource.STOCK
        )
        assertThat(contentPartnerResource.ingest).isEqualTo(
            IngestDetailsResource(
                type = IngestType.MRSS,
                urls = listOf("https://feed.mrss")
            )
        )
        assertThat(contentPartnerResource.deliveryFrequency).isEqualTo(Period.ofMonths(3))
        assertThat(contentPartnerResource.contractId).isEqualTo("id")
        assertThat(contentPartnerResource.contractName).isEqualTo("TED")
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
            hubspotId = "12345678d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            deliveryFrequency = Period.ofMonths(3),
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
        assertThat(contentPartnerResource.hubspotId).isNull()
        assertThat(contentPartnerResource.awards).isNull()
        assertThat(contentPartnerResource.notes).isNull()
        assertThat(contentPartnerResource.language).isNull()
        assertThat(contentPartnerResource.contentTypes).isNull()
        assertThat(contentPartnerResource.ingest).isNull()
        assertThat(contentPartnerResource.deliveryFrequency).isNull()
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
            hubspotId = "12345678d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            deliveryFrequency = Period.ofMonths(3),
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
}
