package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RebuildSearchIndexIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var rebuildSearchIndex: RebuildSearchIndex

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `execute rebuilds search index`() {
        saveVideo(videoId = 1, title = "video")

        rebuildSearchIndex.execute().get()

        assertThat(videoService.search(VideoSearchQuery(text = "video", filters = emptyList(), pageIndex = 0, pageSize = 10))).isNotEmpty
    }
}