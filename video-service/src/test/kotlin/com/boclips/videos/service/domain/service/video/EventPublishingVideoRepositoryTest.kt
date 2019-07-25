package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class EventPublishingVideoRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventPublishingVideoRepository: EventPublishingVideoRepository

    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Test
    fun `publishes a VideoUpdated event when update called`() {
        val videoId = saveVideo()
        val subjects = listOf(subjectRepository.create("Maths"))

        eventPublishingVideoRepository.update(VideoUpdateCommand.ReplaceSubjects(videoId, subjects))

        val event = fakeEventBus.getEventOfType(VideoUpdated::class.java)

        assertThat(event.video.subjects.first().name).isEqualTo("Maths")
    }

    @Test
    fun `publishes VideoUpdated events when bulkUpdate called`() {
        val video1 = saveVideo()
        val video2 = saveVideo()

        val subjects = listOf(subjectRepository.create("Maths"))

        val updateCommands = listOf(VideoUpdateCommand.ReplaceAgeRange(video1, AgeRange.bounded(5, 11)), VideoUpdateCommand.ReplaceSubjects(video2, subjects))

        eventPublishingVideoRepository.bulkUpdate(updateCommands)

        assertThat(fakeEventBus.countEventsOfType(VideoUpdated::class.java)).isEqualTo(2)
    }

    @Test
    fun `publishes a VideoCreated event when video is created`() {
        eventPublishingVideoRepository.create(TestFactories.createVideo())

        assertThat(fakeEventBus.countEventsOfType(VideoCreated::class.java)).isEqualTo(1)
    }
}
