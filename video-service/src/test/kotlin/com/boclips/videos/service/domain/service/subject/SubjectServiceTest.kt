package com.boclips.videos.service.domain.service.subject

import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var collectionRetrievalService: CollectionRetrievalService

    @Autowired
    lateinit var subjectService: SubjectService

    @Nested
    inner class RemoveSubject {
        @Test
        fun `update subject in all collections, videos and indices`() {
            val user = UserFactory.sample()
            val savedSubject = saveSubject("some-subject")
            val existingCollection = saveCollection(subjects = setOf(savedSubject))
            val existingVideo = saveVideo(subjectIds = setOf(savedSubject.id.value))

            subjectService.removeReferences(savedSubject.id, user)

            val retrievedCollection = collectionRepository.find(existingCollection)!!
            assertThat(retrievedCollection.subjects.map { it.name }).doesNotContain("some-subject")

            val retrievedVideo = videoRepository.find(existingVideo)!!
            assertThat(retrievedVideo.subjects.items.map { it.name }).doesNotContain("some-subject")
        }
    }

    @Nested
    inner class UpdateSubject {
        @Test
        fun `update subject in all collections, videos and indices`() {
            val savedSubject = saveSubject("some-subject")
            val existingCollection = saveCollection(subjects = setOf(savedSubject))
            val existingVideo = saveVideo(subjectIds = setOf(savedSubject.id.value))

            subjectService.replaceReferences(savedSubject.copy(name = "changed-name"))

            val retrievedCollection = collectionRepository.find(existingCollection)!!
            assertThat(retrievedCollection.subjects.map { it.name }).containsExactly("changed-name")

            val retrievedVideo = videoRepository.find(existingVideo)!!
            assertThat(retrievedVideo.subjects.items.map { it.name }).containsExactly("changed-name")
        }
    }
}

