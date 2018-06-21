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
class VideoAnalysisServiceStagingE2E {
    @Autowired
    lateinit var videoAnalysisService: VideoAnalysisService

    @Test
    fun countAllVideosFromKaltura() {
        assertThat(videoAnalysisService.countAllKalturaVideos()).isGreaterThan(28000)
    }

    @Test
    fun getAllReadyVideosFromKaltura() {
        val allVideoIds = videoAnalysisService.getReadyVideosFromKaltura()

        assertThat(allVideoIds.size).isGreaterThan(2000)
        assertThat(allVideoIds.size).isLessThan(5000)
    }

    @Test
    fun getAllFaultyVideosFromKaltura() {
        val allVideoIds = videoAnalysisService.getFaultyVideosFromKaltura()

        assertThat(allVideoIds.size).isGreaterThan(20000)
    }

    @Test
    fun getAllVideosFromKaltura() {
        val allVideoIds = videoAnalysisService.getAllVideosFromKaltura()

        assertThat(allVideoIds.size).isGreaterThan(30000)
    }

    @Test
    fun getAllVideosFromBoclips() {
        assertThat(videoAnalysisService.countAllBoclipsVideos()).isGreaterThan(100)
    }

    @Test
    fun getAllPlayableVideos_videosInBoclipsAndInKaltura() {
        val playableVideos = videoAnalysisService.getPlayableVideos()

        assertThat(playableVideos.size).isGreaterThan(100)
    }

    @Test
    fun getAllUnplayableVideos_videosInBoclipsNotInKaltura() {
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()

        assertThat(unplayableVideos).hasSize(0)
    }

    @Test
    fun getFreeableVideos_videosInKalturaNotInBoclips() {
        val freeableVideos = videoAnalysisService.getFreeableVideos()

        assertThat(freeableVideos.size).isEqualTo(0)
    }
}