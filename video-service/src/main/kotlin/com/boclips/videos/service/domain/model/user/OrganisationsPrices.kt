package com.boclips.videos.service.domain.model.user

import com.boclips.videos.service.domain.model.video.Price

data class OrganisationsPrices (
    val prices: Map<OrganisationId, Price>?,
    val default: Price
)
