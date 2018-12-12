package com.boclips.videos.service.infrastructure.video.subject

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectRepositoryTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: SubjectRepository

    val videoId = 1L

    @BeforeEach
    fun setUp() {
        saveVideo(videoId = videoId)
    }

    @Test
    fun `add new subjects`() {
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Maths")))

        val subjects = subjectRepository.findByVideoIds(listOf(1))

        assertThat(subjects).hasSize(1)
    }

    @Test
    fun `add an existing subject`() {
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Maths")))
        subjectRepository.setSubjectsForVideo(videoId, listOf("Maths"))

        val subjects = subjectRepository.findByVideoIds(listOf(videoId))

        assertThat(subjects).hasSize(1)
    }

    @Test
    fun `add subjects appends to existing subjects`() {
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Maths"), VideoSubjectEntity(videoId = videoId, subjectName = "Physics")))
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Art")))

        val subjects = subjectRepository.findByVideoIds(listOf(videoId))

        assertThat(subjects).hasSize(3)
    }

    @Test
    fun `change an existing subject`() {
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Maths"), VideoSubjectEntity(videoId = videoId, subjectName = "Physics")))
        subjectRepository.setSubjectsForVideo(videoId, listOf("Physics"))

        val subjects = subjectRepository.findByVideoIds(listOf(videoId))

        assertThat(subjects).containsExactly(VideoSubjectEntity(videoId = videoId, subjectName = "Physics"))
    }
}