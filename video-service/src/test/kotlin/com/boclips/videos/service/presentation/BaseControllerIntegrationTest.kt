package com.boclips.videos.service.presentation

import com.boclips.security.testing.setSecurityContext
import com.boclips.security.testing.setSecurityContextWithClientId
import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.feature.FeatureKeyResource
import com.boclips.videos.service.domain.model.AccessRules
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.video.VideoAccess
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
            setSecurityContextWithClientId(userId = "a valid user", clientId = "teachers")

            val teachersVideo = saveVideo(title = "A video")
            val hqVideo = saveVideo(title = "A different video")
            addAccessToVideoIds("a valid user", teachersVideo.value, client = "teachers")
            addAccessToVideoIds("a valid user", hqVideo.value, client = "hq")

            val user = controller.getCurrentUser()
            val accessRules = user.accessRules

            assertThat((accessRules.videoAccess as? VideoAccess.Rules)?.accessRules).containsExactly(
                VideoAccessRule.IncludedIds(
                    videoIds = setOf(teachersVideo)
                )
            )
        }

        @Test
        fun `user's features are retrieved from user service`() {
            setSecurityContext("hello")
            usersClient.add(
                UserResourceFactory.sample(
                    id = "hello",
                    organisation = OrganisationResourceFactory.sampleDetails(
                        features = mapOf(FeatureKeyResource.BO_WEB_APP_PRICES.toString() to true)
                    )
                )
            )

            val user = controller.getCurrentUser()
            assertThat(user.isPermittedToViewPrices).isTrue()
        }

        @Test
        fun `anonymous users are given access to videos and public collections`() {
            val user = controller.getCurrentUser()

            assertThat(user.accessRules).isEqualTo(
                AccessRules(
                    videoAccess = VideoAccess.Everything,
                    collectionAccess = CollectionAccessRule.everything()
                )
            )
        }
    }
}
