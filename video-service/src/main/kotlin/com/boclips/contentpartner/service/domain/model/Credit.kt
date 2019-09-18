package com.boclips.contentpartner.service.domain.model

sealed class Credit {
    data class YoutubeCredit(val channelId: String) : Credit()
    object PartnerCredit : Credit()
}
