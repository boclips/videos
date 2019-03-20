package com.boclips.videos.service.application.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.EventService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Test

internal class AnalyseVideoTest {

    @Test
    internal fun `sends an event`() {
        val id = TestFactories.aValidId()
        val video = TestFactories.createVideo()
        val videoService = mock<VideoService>()
        whenever(videoService.get(AssetId(id))).thenReturn(video)
        val eventService = mock<EventService>()
        val analyseVideo = AnalyseVideo(videoService, eventService)

        analyseVideo(id)

        verify(eventService).analyseVideo(video)
    }
}