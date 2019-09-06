package com.boclips.videos.service.application.subject

import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateSubjectIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateSubject: UpdateSubject

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `publishes subject updated event`() {
        val savedSubjectId = saveSubject("Maths").id

        updateSubject.invoke(savedSubjectId, "Mathematicus")

        assertThat(fakeEventBus.countEventsOfType(SubjectChanged::class.java)).isEqualTo(1)
        assertThat((fakeEventBus.receivedEvents[0] as SubjectChanged).subject.name).isEqualTo("Mathematicus")
    }

    @Test
    fun `updates correct subject in videos`() {
        val mathsSubject = saveSubject("Maths")
        val englishSubject = saveSubject("English")
        val savedVideo = saveVideo(subjectIds = setOf(mathsSubject.id.value, englishSubject.id.value))

        fakeEventBus.publish(
            SubjectChanged.builder()
                .subject(
                    Subject.builder()
                        .name("MathIsFun")
                        .id(SubjectId.builder().value(mathsSubject.id.value).build())
                        .build()
                )
                .build()
        )

        val updatedVideo = videoRepository.find(savedVideo)!!

        val newSubject = com.boclips.videos.service.domain.model.subject.Subject(
            id = mathsSubject.id,
            name = "MathIsFun"
        )

        assertThat(updatedVideo.subjects).containsExactlyInAnyOrder(newSubject, englishSubject)
    }
}
