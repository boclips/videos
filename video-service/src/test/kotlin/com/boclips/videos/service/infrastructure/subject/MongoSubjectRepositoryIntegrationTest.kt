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
        mongoSubjectRepository.create(name = "Mathematics")
        mongoSubjectRepository.create(name = "French")

        val subjects = mongoSubjectRepository.findAll()

        assertThat(subjects).hasSize(2)
        assertThat(subjects.first().id).isNotNull
        assertThat(subjects.first().name).isEqualTo("Mathematics")
    }

    @Test
    fun `create a subject`() {
        mongoSubjectRepository.create(name = "Mathematics")

        val subjects = mongoSubjectRepository.findAll()

        assertThat(subjects).hasSize(1)
        assertThat(subjects.first().id).isNotNull
        assertThat(subjects.first().name).isEqualTo("Mathematics")
    }
}
