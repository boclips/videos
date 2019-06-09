package com.boclips.videos.service.application.video

import com.boclips.events.types.Subject
import com.boclips.events.types.video.VideoSubjectClassified
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder


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

        subscriptions.videoSubjectClassified().send(MessageBuilder.withPayload(event).build())

        val video = videoRepository.find(videoId)!!
        assertThat(video.subjects).containsExactly(maths)
    }
}
