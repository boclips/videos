package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.ContentPackageMetricsClient
import com.boclips.videos.api.request.admin.VideosForContentPackageParams
import com.boclips.videos.api.response.video.VideoIdResource
import com.boclips.videos.api.response.video.VideoIdsResource
import com.boclips.videos.api.response.video.VideoIdsWrapper

class ContentPackageMetricsClientFake : ContentPackageMetricsClient, FakeClient<VideoIdResource> {
    private var ids: List<VideoIdResource> = emptyList()

    override fun getVideosForContentPackage(
        id: String,
        params: VideosForContentPackageParams
    ): VideoIdsResource = VideoIdsResource(
        ids.map { it.value }.let(::VideoIdsWrapper),
        emptyMap()
    )

    override fun add(element: VideoIdResource): VideoIdResource {
        ids = listOf(element) + ids
        return element
    }

    override fun findAll() = ids

    override fun clear() {
        ids = emptyList()
    }
}
