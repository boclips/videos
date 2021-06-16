package com.boclips.videos.service.application.subject

import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
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
        val anotherSavedVideo = saveVideo(subjectIds = setOf(englishSubject.id.value))

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

        assertThat(videoRepository.find(savedVideo)!!.subjects.items.map { it.name })
            .containsExactlyInAnyOrder("MathIsFun", "English")
        assertThat(videoRepository.find(anotherSavedVideo)!!.subjects.items.map { it.name })
            .containsExactlyInAnyOrder("English")
    }

    @Test
    fun `updates correct subject in collections`() {
        val mathsSubject = saveSubject("Maths")
        val englishSubject = saveSubject("English")
        val savedCollection = saveCollection(subjects = setOf(mathsSubject, englishSubject))
        val anotherSavedCollection = saveCollection(subjects = setOf(englishSubject))

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

        val updatedCollection = collectionRepository.find(savedCollection)!!
        val anotherCollection = collectionRepository.find(anotherSavedCollection)!!

        assertThat(updatedCollection.subjects.map { it.name }).containsExactlyInAnyOrder("MathIsFun", "English")
        assertThat(anotherCollection.subjects.map { it.name }).containsExactlyInAnyOrder("English")
    }
}
