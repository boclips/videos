package com.boclips.videos.service.application.video

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.domain.model.FixedAgeRange
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateVideoSubjectsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var updateVideo: UpdateVideo

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
        assertThat(video.subjects.items).containsExactly(maths)
    }

    @Test
    fun `matching fields are updated, subjects and subjectsWereSetManually included`() {
        val videoId = saveVideo(title = "title", description = "description")
        val subjectsList = listOf(
            saveSubject(name = "Design"),
            saveSubject(name = "Art")
        )
        val subjectIdList = subjectsList.map { it.id.value }

        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(
                title = null,
                description = "new description",
                promoted = true,
                subjectIds = subjectIdList,
                ageRangeMin = 3,
                ageRangeMax = 7,
                rating = 4
            ),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.title).isEqualTo("title")
        assertThat(updatedVideo.description).isEqualTo("new description")
        assertThat(updatedVideo.promoted).isEqualTo(true)
        assertThat(updatedVideo.subjects.items).containsExactlyInAnyOrder(*subjectsList.toTypedArray())
        assertThat(updatedVideo.subjects.setManually).isTrue()
        assertThat(updatedVideo.ageRange).isEqualTo(FixedAgeRange(min = 3, max = 7, curatedManually = true))
    }

    @Test
    fun `with no subjects specified, subjectsWereSetManually stays false`() {
        val videoId = saveVideo(
            title = "title",
            description = "description",
            subjectIds = emptySet()
        )
        updateVideo(
            id = videoId.value,
            updateRequest = VideoServiceApiFactory.createUpdateVideoRequest(
                title = null,
                description = "new description",
                promoted = true,
                subjectIds = null
            ),
            user = UserFactory.sample(id = "admin@boclips.com")
        )

        val updatedVideo = videoRepository.find(videoId)!!

        assertThat(updatedVideo.subjects.items).isEmpty()
        assertThat(updatedVideo.subjects.setManually).isFalse()
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
        assertThat(video.subjects.items).containsExactly(subject)
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
