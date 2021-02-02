package com.boclips.search.service.domain.videos.model

import java.math.BigDecimal

data class PricesFilter(
    val userOrganisationId: String? = null,
    val queriedPrices: Set<BigDecimal> = emptySet()
) {

    fun shouldFilter() = !userOrganisationId.isNullOrEmpty() && queriedPrices.isNotEmpty()
}
