package com.boclips.videos.service.infrastructure.organisation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.users.api.factories.OrganisationResourceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiOrganisationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var organisationService: ApiOrganisationService

    @Test
    fun `gets all organisations with custom prices`() {
        val orgWithPrices =
            organisationsClient.add(OrganisationResourceFactory.sample(deal = OrganisationResourceFactory.sampleDeal()))
        organisationsClient.add(OrganisationResourceFactory.sample())

        val pricedOrgs = organisationService.getOrganisationsWithCustomPrices()

        assertThat(pricedOrgs).hasSize(1)
        assertThat(pricedOrgs.first().organisationId.value).isEqualTo(orgWithPrices.id)
    }
}
