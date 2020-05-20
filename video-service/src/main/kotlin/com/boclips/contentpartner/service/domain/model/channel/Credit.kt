package com.boclips.contentpartner.service.domain.model.channel

sealed class Credit {
    data class YoutubeCredit(val channelId: String) : Credit()
    object PartnerCredit : Credit()
}
