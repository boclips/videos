package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId

object OrganisationFactory {

    fun sample(
        organisationId: OrganisationId = OrganisationId("org-id"),
        deal: Deal = Deal(prices = DealPricesFactory.sample()),
        allowOverridingUserIds: Boolean = false,
        hasAccessToPrices: Boolean = true
    ): Organisation {
        return Organisation(
            organisationId = organisationId,
            allowOverridingUserIds = allowOverridingUserIds,
            deal = deal,
            hasAccessToPrices = hasAccessToPrices
        )
    }
}
