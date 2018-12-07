package com.boclips.videos.service.client

interface VideoServiceClient {

    fun create(request: CreateVideoRequest)

    fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean
}
