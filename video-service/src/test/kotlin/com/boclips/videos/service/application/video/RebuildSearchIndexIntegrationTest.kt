package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class RebuildSearchIndexIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var rebuildSearchIndex: RebuildSearchIndex

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `execute rebuilds search index creates an index with videos filtered for teachers`() {
        saveVideo(videoId = 1, title = "first")
        saveVideo(videoId = 2, title = "stock", typeId = 2)
        saveVideo(videoId = 3, title = "third")

        rebuildSearchIndex.execute().get()

        assertThat(videoService.findVideosBy(VideoSearchQuery(text = "first"))).isNotEmpty
        assertThat(videoService.findVideosBy(VideoSearchQuery(text = "stock"))).isEmpty()
        assertThat(videoService.findVideosBy(VideoSearchQuery(text = "third"))).isNotEmpty
    }
}