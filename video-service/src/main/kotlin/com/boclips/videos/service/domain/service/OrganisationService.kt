package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.user.Organisation

interface OrganisationService {
    fun getOrganisationsWithCustomPrices(): List<Organisation>
}
