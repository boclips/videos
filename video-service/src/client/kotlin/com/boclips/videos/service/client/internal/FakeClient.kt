package com.boclips.videos.service.client.internal

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.VideoServiceClient

internal class FakeClient : VideoServiceClient {

    private val videos: MutableList<CreateVideoRequest> = mutableListOf()

    override fun create(request: CreateVideoRequest) {
        videos += request
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean {
        return videos.any { it.provider == contentPartnerId && it.providerVideoId == contentPartnerVideoId }
    }
}
