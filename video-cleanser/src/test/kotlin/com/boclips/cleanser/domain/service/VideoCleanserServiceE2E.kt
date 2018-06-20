package com.boclips.cleanser.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("staging")
class VideoCleanserServiceE2E {
    @Autowired
    lateinit var videoCleanserService: CleanserService

    @Test
    fun countAllVideosFromKaltura() {
        assertThat(videoCleanserService.countAllKalturaVideos()).isGreaterThan(28000)
    }

    @Test
    fun getAllReadyVideosFromKaltura() {
        val allVideoIds = videoCleanserService.getReadyVideosFromKaltura()

        assertThat(allVideoIds.size).isGreaterThan(2000)
        assertThat(allVideoIds.size).isLessThan(10000)
    }

    @Test
    fun getAllFaultyVideosFromKaltura() {
        val allVideoIds = videoCleanserService.getFaultyVideosFromKaltura()

        assertThat(allVideoIds.size).isGreaterThan(10000)
    }

    @Test
    fun getAllUnplayableVideos() {
        val unplayableVideos = videoCleanserService.getUnplayableVideos()

        assertThat(unplayableVideos).hasSize(0)
    }
}