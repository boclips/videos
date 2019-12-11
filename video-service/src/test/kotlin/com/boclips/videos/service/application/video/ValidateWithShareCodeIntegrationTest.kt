package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.InvalidShareCodeException
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class ValidateWithShareCodeIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    lateinit var validateShareCode: ValidateWithShareCode

    @BeforeEach
    fun setup() {
        validateShareCode = ValidateWithShareCode(videoService)
    }

    @Test
    fun `check valid shareCode does not throw`() {
        val video = TestFactories.createVideo(videoId = ObjectId().toHexString(), shareCodes = setOf("BCD2", "GTHY"))
        videoService.create(video)
        validateShareCode(video.videoId.value, "BCD2")
    }

    @Test
    fun `check with an invalid shareCode does throw`() {
        val video = TestFactories.createVideo(videoId = ObjectId().toHexString(), shareCodes = setOf("BCD2", "GTHY"))
        videoService.create(video)
        assertThrows<InvalidShareCodeException> {
            validateShareCode(video.videoId.value, "ABCD")
        }
    }
}
