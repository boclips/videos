package com.boclips.videos.service.infrastructure.organisation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.*

class ApiOrganisationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var organisationService: ApiOrganisationService

    @Test
    fun `gets all organisations with custom prices`() {
        val orgWithPrices =
            organisationsClient.add(
                OrganisationResourceFactory.sample(
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
                        ),
                    )
                )
            )
        organisationsClient.add(OrganisationResourceFactory.sample())

        val pricedOrgs = organisationService.getOrganisationsWithCustomPrices()

        assertThat(pricedOrgs).hasSize(1)
        assertThat(pricedOrgs.first().organisationId.value).isEqualTo(orgWithPrices.id)
        assertThat(pricedOrgs.first().deal.prices.channelPrices[ChannelId("getty-id")]?.amount).isEqualTo(BigDecimal.valueOf(500))
        assertThat(pricedOrgs.first().deal.prices.channelPrices[ChannelId("getty-id")]?.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(pricedOrgs.first().deal.prices.videoTypePrices[VideoType.STOCK]?.amount).isEqualTo(BigDecimal.valueOf(10))
        assertThat(pricedOrgs.first().deal.prices.videoTypePrices[VideoType.STOCK]?.currency).isEqualTo(Currency.getInstance("USD"))
    }
}
