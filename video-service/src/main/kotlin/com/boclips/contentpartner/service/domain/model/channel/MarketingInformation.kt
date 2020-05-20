package com.boclips.contentpartner.service.domain.model.channel

import java.net.URL

data class MarketingInformation(
    val oneLineDescription: String? = null,
    val status: ChannelStatus? = null,
    val logos: List<URL>? = listOf(),
    val showreel: URL? = null,
    val sampleVideos: List<URL>? = listOf()
)
