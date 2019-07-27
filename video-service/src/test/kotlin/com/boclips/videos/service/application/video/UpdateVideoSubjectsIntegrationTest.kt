package com.boclips.videos.service.application.video

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateVideoSubjectsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Test
    fun `stores subjects`() {
        val videoId = saveVideo()
        val maths = subjectRepository.create("Maths")
        val subjectTag = SubjectId(maths.id.value)
        val event = VideoSubjectClassified.builder()
            .videoId(videoId.value)
            .subjects(setOf(subjectTag))
            .build()

        fakeEventBus.publish(event)

        val video = videoRepository.find(videoId)!!
        assertThat(video.subjects).containsExactly(maths)
    }

    @Test
    fun `does not update with invalid subject ids`() {
        val videoId = saveVideo()
        val subject = subjectRepository.create("Maths")
        setVideoSubjects(videoId.value, subject.id)

        val unrecognisedSubject = SubjectId(TestFactories.aValidId())

        val event = VideoSubjectClassified.builder()
            .videoId(videoId.value)
            .subjects(setOf(unrecognisedSubject))
            .build()

        fakeEventBus.publish(event)

        val video = videoRepository.find(videoId)!!
        assertThat(video.subjects).containsExactly(subject)
    }

    @Test
    fun `fires an event when subjects are updated`() {
        val videoId = saveVideo()
        val maths = subjectRepository.create("Maths")

        val subjectTag = SubjectId(maths.id.value)

        val event = VideoSubjectClassified.builder()
            .videoId(videoId.value)
            .subjects(setOf(subjectTag))
            .build()

        fakeEventBus.publish(event)

        val publishedEvent = fakeEventBus.getEventOfType(VideoUpdated::class.java)

        assertThat(publishedEvent.video.subjects.first().name).isEqualTo("Maths")
    }
}
