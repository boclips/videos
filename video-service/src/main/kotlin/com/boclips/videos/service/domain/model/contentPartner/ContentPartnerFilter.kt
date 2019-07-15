package com.boclips.videos.service.domain.model.contentPartner

sealed class ContentPartnerFilter {
    data class NameFilter(val name: String) : ContentPartnerFilter()
    data class OfficialFilter(val isOfficial: Boolean) : ContentPartnerFilter()
    data class AccreditedTo(val credit: Credit) : ContentPartnerFilter()
}