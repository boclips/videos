package com.boclips.contentpartner.service.domain.model

sealed class ContentPartnerFilter {
    data class NameFilter(val name: String) : ContentPartnerFilter()
    data class OfficialFilter(val official: Boolean) : ContentPartnerFilter()
    data class AccreditedTo(val credit: Credit) : ContentPartnerFilter()
    data class HubspotIdFilter(val hubspotId: String) : ContentPartnerFilter()
    data class IngestTypesFilter(val ingestTypes: List<String>) : ContentPartnerFilter()
}
