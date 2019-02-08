package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.VideoServiceClient
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

internal class VideoServiceHealthIndicatorTest {

    val videoServiceClient = mock<VideoServiceClient>()
    val subject = VideoServiceHealthIndicator(videoServiceClient)

    @Test
    fun `not found means healthy`() {
        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenReturn(false)

        assertThat(subject.health().status).isEqualTo(Status.UP)
    }

    @Test
    fun `found means healthy`() {
        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenReturn(true)

        assertThat(subject.health().status).isEqualTo(Status.UP)
    }

    @Test
    fun `exception means unhealthy`() {
        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenThrow(
            HttpClientErrorException(
                HttpStatus.BAD_REQUEST
            )
        )

        assertThat(subject.health().status).isEqualTo(Status.DOWN)
    }
}