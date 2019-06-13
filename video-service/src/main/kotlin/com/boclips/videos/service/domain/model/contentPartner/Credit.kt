package com.boclips.videos.service.domain.model.contentPartner

sealed class Credit {
    data class YoutubeCredit(val channelId: String) : Credit()
    object PartnerCredit : Credit()
}