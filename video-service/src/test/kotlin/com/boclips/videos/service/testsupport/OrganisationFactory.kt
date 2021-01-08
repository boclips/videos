package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import java.util.*

object OrganisationFactory {

    fun sample(
            organisationId: OrganisationId = OrganisationId(UUID.randomUUID().toString()),
            deal: Deal = DealFactory.sample(),
            allowOverridingUserIds: Boolean = false
    ) = Organisation(
            organisationId = organisationId,
            deal = deal,
            allowOverridingUserIds = allowOverridingUserIds
    )

    object DealFactory {

        fun sample(prices: Deal.Prices = Deal.Prices(videoTypePrices = emptyMap())) =
                Deal(prices = prices)
    }
}
