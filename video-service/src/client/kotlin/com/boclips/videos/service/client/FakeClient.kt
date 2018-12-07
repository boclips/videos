package com.boclips.videos.service.client

class FakeClient : VideoServiceClient {

    private val videos: MutableList<CreateVideoRequest> = mutableListOf()

    override fun create(request: CreateVideoRequest) {
        videos += request
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean {
        return videos.any { it.provider == contentPartnerId && it.providerVideoId == contentPartnerVideoId }
    }
}
