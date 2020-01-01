package com.boclips.videos.service.client.spring

//import com.boclips.videos.service.client.VideoServiceClient
//import com.nhaarman.mockitokotlin2.any
//import com.nhaarman.mockitokotlin2.mock
//import com.nhaarman.mockitokotlin2.whenever
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//import org.springframework.http.HttpStatus
//import org.springframework.web.client.HttpClientErrorException
//
//internal class VideoServiceHealthIndicatorTest {
//    private val videoServiceClient = mock<VideoServiceClient>()
//    private val healthChecker = VideoServiceHealthIndicator(videoServiceClient)
//
//    @Test
//    fun `not found means healthy`() {
//        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenReturn(false)
//
//        assertThat(healthChecker.health().status).isEqualTo("UP")
//    }
//
//    @Test
//    fun `found means healthy`() {
//        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenReturn(true)
//
//        assertThat(healthChecker.health().status).isEqualTo("UP")
//    }
//
//    @Test
//    fun `exception means unhealthy`() {
//        whenever(videoServiceClient.existsByContentPartnerInfo(any(), any())).thenThrow(
//            HttpClientErrorException(
//                HttpStatus.BAD_REQUEST
//            )
//        )
//
//        assertThat(healthChecker.health().status).isEqualTo("DOWN")
//    }
//}
