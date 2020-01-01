package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.api.request.video.TagVideoRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TagVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `tag video`(
        @Autowired tagRepository: TagRepository,
        @Autowired videoRepository: VideoRepository
    ) {
        val tagVideo = TagVideo(videoRepository, tagRepository)

        val originalVideo = TestFactories.createVideo()
        videoRepository.create(originalVideo)
        val tag = tagRepository.create("my tag")

        tagVideo(
            TagVideoRequest(
                "${originalVideo.videoId}",
                "https://example.com/tags/${tag.id.value}"
            ),
            UserFactory.sample()
        )

        val taggedVideo = videoRepository.find(originalVideo.videoId)!!
        assertThat(taggedVideo.tag!!.tag.label).isEqualTo("my tag")
    }
}
