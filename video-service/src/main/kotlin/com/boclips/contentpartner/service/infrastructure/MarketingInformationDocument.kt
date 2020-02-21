package com.boclips.contentpartner.service.infrastructure

import java.net.URL

data class MarketingInformationDocument(
    val oneLineDescription: String? = null,
    val status: ContentPartnerStatusDocument? = null,
    val logos: List<String>? = null,
    val showreel: String? = null,
    val sampleVideos: List<String>? = null
)

enum class ContentPartnerStatusDocument {
    NEEDS_INTRODUCTION,
    HAVE_REACHED_OUT,
    NEEDS_CONTENT,
    WAITING_FOR_INGEST,
    SHOULD_ADD_TO_SITE,
    SHOULD_PROMOTE,
    PROMOTED
}
