package com.boclips.videos.service.application.subject

import com.boclips.videos.service.domain.model.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.subject.SubjectRepository
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteSubjectIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var subjectRepository: SubjectRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionService: CollectionService

    @Autowired
    lateinit var deleteSubject: DeleteSubject

    @Test
    fun `when deleting subject, it deletes the subject from collections and updates index`() {
        val subjectId = subjectRepository.create("Biology").id
        val collectionWithSubject = saveCollection(subjects = setOf(subjectId.value))

        deleteSubject(subjectId)

        assertThat(subjectRepository.findById(subjectId)).isNull()

        val collection = collectionRepository.find(collectionWithSubject)!!
        assertThat(collection.subjects).doesNotContain(subjectId)

        assertThat(
            collectionService.count(
                CollectionSearchQuery(
                    subjectIds = listOf(subjectId.value),
                    text = null,
                    pageIndex = 0,
                    pageSize = 10
                )
            )
        ).isEqualTo(0)
    }
}
