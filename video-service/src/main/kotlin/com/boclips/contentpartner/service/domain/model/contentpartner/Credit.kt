package com.boclips.contentpartner.service.domain.model.contentpartner

sealed class Credit {
    data class YoutubeCredit(val channelId: String) : Credit()
    object PartnerCredit : Credit()
}
