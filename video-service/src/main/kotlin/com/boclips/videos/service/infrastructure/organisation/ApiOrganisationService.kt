package com.boclips.videos.service.infrastructure.organisation

import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.request.OrganisationFilterRequest
import com.boclips.users.api.response.organisation.OrganisationsResource
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.service.OrganisationService

class ApiOrganisationService(
    val organisationsClient: OrganisationsClient
) : OrganisationService {
    override fun getOrganisationsWithCustomPrices(): List<Organisation> {
        val firstPage = organisationsClient.getOrganisations(
            filterRequest = OrganisationFilterRequest().apply {
                hasCustomPrices = true
            }
        )

        if (firstPage.page == null) {
            return firstPage._embedded.organisations.map { OrganisationResourceConverter.convertOrganisation(it) }
        }

        return fetchMoreResults(firstPage)
    }

    /**
     * TODO FIX PAGING IN FAKE
     */
    private fun fetchMoreResults(firstPage: OrganisationsResource): List<Organisation> {
        return if (firstPage.page!!.totalElements > firstPage.page!!.size) {
            val secondPage = organisationsClient.getOrganisations(
                filterRequest = OrganisationFilterRequest().apply {
                    hasCustomPrices = true
                    size = (firstPage.page!!.totalElements - firstPage.page!!.size).toInt()
                    page = 1
                }
            )

            (secondPage._embedded.organisations + firstPage._embedded.organisations).map {
                OrganisationResourceConverter.convertOrganisation(
                    it
                )
            }
        } else {
            firstPage._embedded.organisations.map { OrganisationResourceConverter.convertOrganisation(it) }
        }
    }
}
