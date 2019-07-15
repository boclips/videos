package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter

object ContentPartnerFiltersConverter {
    fun convert(name: String? = null, isOfficial: Boolean? = null): List<ContentPartnerFilter> =
        listOfNotNull(
            getNameFilter(name),
            getOfficialFilter(isOfficial)
        )

    private fun getNameFilter(name: String?) =
        name?.let { ContentPartnerFilter.NameFilter(name = it) }

    private fun getOfficialFilter(isOfficial: Boolean?) =
        isOfficial?.let { ContentPartnerFilter.OfficialFilter(isOfficial = it) }
}
