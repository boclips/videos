package com.boclips.videos.service.presentation

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BaseControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var controller: VideoController

    @Nested
    inner class CurrentUser {
        @Test
        fun `access rules are retrieved from user service`() {
            setSecurityContext("a valid user")

            val video = saveVideo(title = "A video")
            createSelectedVideosContract(video.value)

            val user = controller.getCurrentUser()
            val accessRules = user.accessRules

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccessRule.SpecificIds(videoIds = setOf(video)))
        }

        @Test
        fun `anonymous users are given access to videos and public collections`() {
            val user = controller.getCurrentUser()

            assertThat(user.accessRules).isEqualTo(
                AccessRules(
                    videoAccess = VideoAccessRule.Everything,
                    collectionAccess = CollectionAccessRule.public()
                )
            )
        }
    }
}
