package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder
import java.time.Period
import java.util.Currency
import java.util.Locale

class ContentPartnerToResourceConverterTest {
    lateinit var contentPartnerToResourceConverter: ContentPartnerToResourceConverter

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        contentPartnerToResourceConverter =
            ContentPartnerToResourceConverter(
                contentPartnersLinkBuilder = ContentPartnersLinkBuilder(
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
        val contentPartner = createContentPartner(
            credit = Credit.PartnerCredit,
            legalRestriction = ContentPartnerFactory.createLegalRestrictions(text = "Forbidden in the EU"),
            distributionMethods = setOf(DistributionMethod.STREAM),
            remittance = Remittance(
                Currency.getInstance("GBP")
            ),
            description = "this is a description",
            contentCategories = listOf("ANIMATION"),
            hubspotId = "12345678d",
            awards = "first award",
            notes = "first note",
            language = Locale.forLanguageTag("spa"),
            ingest = MrssFeedIngest(
                urls = listOf("https://feed.mrss")
            ),
            deliveryFrequency = Period.ofMonths(3),
            contentTypes = listOf(ContentPartnerType.INSTRUCTIONAL, ContentPartnerType.STOCK)
        )

        val contentPartnerResource = contentPartnerToResourceConverter.convert(contentPartner)

        assertThat(contentPartnerResource.id).isNotEmpty()
        assertThat(contentPartnerResource.name).isNotEmpty()
        assertThat(contentPartnerResource.ageRange).isNotNull
        assertThat(contentPartnerResource.official).isTrue()
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
        assertThat(contentPartnerResource.contentTypes).containsExactlyInAnyOrder("INSTRUCTIONAL", "STOCK")
        assertThat(contentPartnerResource.ingest).isEqualTo(IngestDetailsResource(type = IngestType.MRSS, urls = listOf("https://feed.mrss")))
        assertThat(contentPartnerResource.deliveryFrequency).isEqualTo(Period.ofMonths(3))
    }
}
