package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AnalyseContentPartnerDocumentVideosIntegrationTest(
    @Autowired val analyseContentPartnerVideos: AnalyseContentPartnerVideos
) : AbstractSpringIntegrationTest() {

    @Test
    fun `it should only send analyse messages for Ted`() {
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Bob")

        analyseContentPartnerVideos("Ted", language = null)

        assertThat(fakeEventBus.countEventsOfType(VideoAnalysisRequested::class.java)).isEqualTo(2)
    }
}
