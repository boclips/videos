package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.Subject
import com.boclips.eventbus.events.video.VideoSubjectClassified
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
        val subjectTag = Subject.builder()
            .id(maths.id.value)
            .build()
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

        val subjectTag = Subject.builder()
            .id(TestFactories.aValidId())
            .build()

        val event = VideoSubjectClassified.builder()
            .videoId(videoId.value)
            .subjects(setOf(subjectTag))
            .build()

        fakeEventBus.publish(event)

        val video = videoRepository.find(videoId)!!
        assertThat(video.subjects).containsExactly(subject)
    }
}
