package com.boclips.videos.service.infrastructure.organisation

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.response.feature.FeatureKeyResource
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class OrganisationResourceConverterTest {
    @Test
    fun `converts organisation prices from a resource`() {
        val orgResource = OrganisationResourceFactory.sample(
            id = "my-org-id",
            deal = OrganisationResourceFactory.sampleDeal(
                prices = DealResource.PricesResource(
                    videoTypePrices = mapOf(
                        "STOCK" to DealResource.PriceResource(
                            "10",
                            "USD"
                        )
                    ),
                    channelPrices = mapOf(
                        "getty-id" to DealResource.PriceResource(
                            "500",
                            "USD"
                        )
                    )
                )
            )
        )

        val convertedOrg = OrganisationResourceConverter.convertOrganisation(orgResource)

        assertThat(convertedOrg.organisationId.value).isEqualTo("my-org-id")
        assertThat(convertedOrg.deal.prices.channelPrices[ChannelId("getty-id")]?.amount).isEqualTo(
            BigDecimal.valueOf(
                500
            )
        )
        assertThat(convertedOrg.deal.prices.channelPrices[ChannelId("getty-id")]?.currency).isEqualTo(
            Currency.getInstance(
                "USD"
            )
        )
        assertThat(convertedOrg.deal.prices.videoTypePrices[VideoType.STOCK]?.amount).isEqualTo(BigDecimal.valueOf(10))
        assertThat(convertedOrg.deal.prices.videoTypePrices[VideoType.STOCK]?.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `defaults to empty prices when no custom pricing specified`() {
        val orgResource = OrganisationResourceFactory.sample(
            id = "my-org-id",

            // The factory doesn't allow null :(
            deal = OrganisationResourceFactory.sampleDeal().copy(prices = null)
        )

        val convertedOrg = OrganisationResourceConverter.convertOrganisation(orgResource)
        assertThat(convertedOrg.deal.prices.videoTypePrices).isEmpty()
        assertThat(convertedOrg.deal.prices.channelPrices).isEmpty()
    }

    @Test
    fun `converts organisation access to prices from feature resource`() {
        val orgResource = OrganisationResourceFactory.sample(
            id = "my-org-id",
            organisationDetails = OrganisationResourceFactory.sampleDetails(
                features = mapOf(FeatureKeyResource.BO_WEB_APP_PRICES to false)
            ),
        )

        val convertedOrg = OrganisationResourceConverter.convertOrganisation(orgResource)

        assertThat(convertedOrg.hasAccessToPrices).isFalse()
    }

    @Test
    fun `allows access to prices by default if feature is missing`() {
        val orgResource = OrganisationResourceFactory.sample(
            id = "my-org-id",
            organisationDetails = OrganisationResourceFactory.sampleDetails(
                features = emptyMap()
            ),
        )

        val convertedOrg = OrganisationResourceConverter.convertOrganisation(orgResource)

        assertThat(convertedOrg.hasAccessToPrices).isTrue()
    }
}
