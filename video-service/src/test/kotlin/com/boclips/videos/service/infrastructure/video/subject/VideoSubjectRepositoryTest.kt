package com.boclips.videos.service.infrastructure.video.subject

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoSubjectRepositoryTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: VideoSubjectRepository

    @Test
    fun `add new subjects`() {
        saveVideo(videoId = 1)
        subjectRepository.create(listOf(VideoSubjectEntity(videoId = 1, subjectName = "Maths")))

        val subjects = subjectRepository.findByVideoIdIn(listOf(1))

        assertThat(subjects).hasSize(1)
    }

    @Test
    fun `add an existing subject`() {
        saveVideo(videoId = 1)
        subjectRepository.create(listOf(VideoSubjectEntity(videoId = 1, subjectName = "Maths")))
        subjectRepository.setSubjectsForVideo(1, listOf("Maths"))

        val subjects = subjectRepository.findByVideoIdIn(listOf(1))

        assertThat(subjects).hasSize(1)
    }

    @Test
    fun `change an existing subject`() {
        saveVideo(videoId = 1)

        subjectRepository.create(listOf(VideoSubjectEntity(videoId = 1, subjectName = "Maths"), VideoSubjectEntity(videoId = 1, subjectName = "Physics")))
        subjectRepository.setSubjectsForVideo(1, listOf("Physics"))

        val subjects = subjectRepository.findByVideoIdIn(listOf(1))

        assertThat(subjects).containsExactly(VideoSubjectEntity(videoId = 1, subjectName = "Physics"))
    }
}