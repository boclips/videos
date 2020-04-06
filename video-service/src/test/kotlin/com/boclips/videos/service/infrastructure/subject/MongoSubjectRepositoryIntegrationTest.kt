package com.boclips.videos.service.infrastructure.subject

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectUpdateCommand
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `find by ids`() {
        val maths = mongoSubjectRepository.create(name = "Mathematics")
        val nonExistingSubjectId = TestFactories.aValidId()

        val subjects = mongoSubjectRepository.findByIds(listOf(maths.id.value, nonExistingSubjectId))

        assertThat(subjects).containsExactly(maths)
    }

    @Test
    fun `find by ordered ids`() {
        val maths = mongoSubjectRepository.create(name = "Mathematics")
        val subject2 = mongoSubjectRepository.create(name = "Subject2")
        val subject3 = mongoSubjectRepository.create(name = "Subject3")
        val subject4 = mongoSubjectRepository.create(name = "Subject4")

        val subjects = mongoSubjectRepository.findByOrderedIds(
            listOf(maths.id.value, subject4.id.value, subject2.id.value, subject3.id.value)
        )

        assertThat(subjects[0]).isEqualTo(maths)
        assertThat(subjects[1]).isEqualTo(subject4)
        assertThat(subjects[2]).isEqualTo(subject2)
        assertThat(subjects[3]).isEqualTo(subject3)
    }

    @Test
    fun `create a subject`() {
        mongoSubjectRepository.create(name = "Mathematics")

        val subjects = mongoSubjectRepository.findAll()

        assertThat(subjects).hasSize(1)
        assertThat(subjects.first().id).isNotNull
        assertThat(subjects.first().name).isEqualTo("Mathematics")
    }

    @Test
    fun `delete a subject`() {
        val subject = mongoSubjectRepository.create(name = "Biology")

        mongoSubjectRepository.delete(subject.id)

        assertThat(mongoSubjectRepository.findAll()).isEmpty()
    }

    @Test
    fun `find by Id`() {
        val subject = mongoSubjectRepository.create(name = "Biology")

        val retrievedSubject = mongoSubjectRepository.findById(subject.id)

        assertThat(retrievedSubject).isNotNull
    }

    @Test
    fun `find by name`() {
        mongoSubjectRepository.create(name = "French")

        val retrievedSubject = mongoSubjectRepository.findByName("French")

        assertThat(retrievedSubject!!.name).isEqualTo("French")
    }

    @Test
    fun `update a name of a subject`() {
        val subject = mongoSubjectRepository.create(name = "French")

        val updatedSubject = mongoSubjectRepository.update(
            SubjectUpdateCommand.ReplaceName(subjectId = subject.id, name = "German")
        )

        assertThat(updatedSubject.name).isEqualTo("German")
    }

    @Test
    fun `throws when updating a invalid subject`() {
        assertThrows<IllegalStateException> {
            mongoSubjectRepository.update(
                SubjectUpdateCommand.ReplaceName(
                    subjectId = SubjectId(value = ObjectId().toHexString()),
                    name = "German"
                )
            )
        }
    }
}
