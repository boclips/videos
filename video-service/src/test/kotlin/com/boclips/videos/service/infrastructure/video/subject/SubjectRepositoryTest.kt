package com.boclips.videos.service.infrastructure.video.subject

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class SubjectRepositoryTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: SubjectRepository

    lateinit var videoAssetId: AssetId

    @BeforeEach
    fun setUp() {
        videoAssetId = saveVideo()
    }

    val videoId: Long by lazy {
        videoAssetId.value.toLong()
    }

    @Test
    fun `add new subjects`() {
        subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "Maths")))

        val subjects = subjectRepository.findByVideoIds(listOf(videoId))

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

    @Test
    fun `setSubjectForVideo throws when subject name is empty`() {
        assertThrows<IllegalStateException> {
            subjectRepository.setSubjectsForVideo(videoId, listOf("", "", ""))
        }
    }

    @Test
    fun `add throws when videoId is null`() {
        assertThrows<IllegalStateException> {
            subjectRepository.add(listOf(VideoSubjectEntity(videoId = null, subjectName = "Maths")))
        }
    }

    @Test
    fun `add throws when subjectName is null`() {
        assertThrows<IllegalStateException> {
            subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = null)))
        }
    }

    @Test
    fun `add throws when subjectName is empty`() {
        assertThrows<IllegalStateException> {
            subjectRepository.add(listOf(VideoSubjectEntity(videoId = videoId, subjectName = "")))
        }
    }
}