package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteSubjectIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionRetrievalService: CollectionRetrievalService

    @Autowired
    lateinit var deleteSubject: DeleteSubject

    @Test
    fun `when deleting subject, it deletes the subject from collections and updates index`() {
        val subject = subjectRepository.create("Biology")
        val publicCollectionWithSubject = saveCollection(subjects = setOf(subject), public = true)
        val privateCollectionWithSubject = saveCollection(subjects = setOf(subject), public = false)

        deleteSubject(subject.id, UserFactory.sample())

        assertThat(subjectRepository.findById(subject.id)).isNull()

        val publicCollection = collectionRepository.find(publicCollectionWithSubject)!!
        assertThat(publicCollection.subjects.map { it.id }).doesNotContain(subject.id)

        val privateCollection = collectionRepository.find(privateCollectionWithSubject)!!
        assertThat(privateCollection.subjects.map { it.id }).doesNotContain(subject.id)

        val results = collectionRetrievalService.search(
            CollectionSearchQuery(
                subjectIds = listOf(subject.id.value),
                text = null,
                visibilityForOwners = emptySet(),
                pageIndex = 0,
                pageSize = 10,
                permittedCollections = null,
                hasLessonPlans = null
            ),
            user = UserFactory.sample()
        )

        assertThat(results.elements).isEmpty()
    }
}
