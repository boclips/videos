package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.web.exceptions.BoclipsApiException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

class GetVideoFeedIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getVideoFeed: GetVideoFeed

    @Test
    fun `throws api exception with invalid cursor`() {
        assertThrows<BoclipsApiException> {
            getVideoFeed(cursorId = "invalid", size = 10, user = UserFactory.sample())
        }
    }
}
