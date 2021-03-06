package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.PriceResource
import feign.FeignException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class VideosClientFakeTest {
    @Nested
    inner class GetVideo {
        @Test
        fun `throws a Feign NotFound exception when video is not found`() {
            val fake = VideosClientFake()
            assertThatThrownBy { fake.getVideo("this does not exist") }
                .isInstanceOf(FeignException.NotFound::class.java)
        }
    }

    @Nested
    inner class GetVideoPrice {
        @Test
        fun `throws a Feign NotFound exception when video is not found`() {
            val fake = VideosClientFake()
            fake.addCustomVideoPrice("video-id", PriceResource(amount = BigDecimal.TEN, currency = Currency.getInstance("USD")))
            assertThatThrownBy { fake.getVideoPrice("this does not exist", "a-user-id") }
                .isInstanceOf(FeignException.NotFound::class.java)
        }
    }

    @Nested
    inner class ProbeVideoReference {
        @Test
        fun `throws a Feign NotFound exception when video is not found`() {
            val fake = VideosClientFake()
            assertThatThrownBy { fake.probeVideoReference("this does not exist", "neither does this") }
                .isInstanceOf(FeignException.NotFound::class.java)
        }
    }

    @Nested
    inner class RequestCaptions {
        @Test
        fun `throws a feign Conflict exception when captions are already requested`() {
            val fake = VideosClientFake()
            val videoResource = fake.createVideo(VideoServiceApiFactory.createCreateVideoRequest())
            fake.updateCaptionStatus(videoId = videoResource.id!!, captionStatus = CaptionStatus.REQUESTED)

            assertThatThrownBy {
                fake.requestVideoCaptions(videoResource.id!!)
            }.isInstanceOf(FeignException.Conflict::class.java)
        }
    }
}
