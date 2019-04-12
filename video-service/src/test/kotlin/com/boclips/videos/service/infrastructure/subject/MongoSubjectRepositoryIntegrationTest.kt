package com.boclips.videos.service.infrastructure.subject

import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.getCollection
import org.springframework.beans.factory.annotation.Autowired

class MongoSubjectRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoSubjectRepository: SubjectRepository

    @Test
    fun `find all subjects`() {
        val subjectDocuments = listOf(
            SubjectDocument(id = ObjectId(), name = "Mathematics"),
            SubjectDocument(id = ObjectId(), name = "French")
        )

        mongoClient.getDatabase(DATABASE_NAME)
            .getCollection<SubjectDocument>(MongoSubjectRepository.collectionName)
            .insertMany(subjectDocuments)

        val subjects = mongoSubjectRepository.findAll()

        assertThat(subjects).hasSize(2)
        assertThat(subjects.first().id).isNotNull
        assertThat(subjects.first().name).isEqualTo("Mathematics")
    }
}
