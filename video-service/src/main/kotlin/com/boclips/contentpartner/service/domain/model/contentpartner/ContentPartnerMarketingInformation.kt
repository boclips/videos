package com.boclips.contentpartner.service.domain.model.contentpartner

import java.net.URL

data class ContentPartnerMarketingInformation(
    val oneLineDescription: String? = null,
    val status: ContentPartnerStatus? = null,
    val logos: List<URL>? = listOf(),
    val showreel: URL? = null,
    val sampleVideos: List<URL>? = listOf()
)
