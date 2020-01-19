package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.subject.SubjectNotFoundException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionSubjectsTest : AbstractSpringIntegrationTest() {
    @Test
    fun `retrieves a set of subjects for provided ids`() {
        val math = saveSubject("Math")
        val physics = saveSubject("Physics")

        val subjectDocuments = collectionSubjects.getByIds(math.id, math.id, physics.id)

        assertThat(subjectDocuments).hasSize(2).extracting("id").containsExactlyInAnyOrder(
            ObjectId(math.id.value),
            ObjectId(physics.id.value)
        )
    }

    @Test
    fun `throws a not found exception when any of given subjects does not exist`() {
        val math = saveSubject("Math")
        val phantomSubject = SubjectId(ObjectId().toHexString())

        assertThrows<SubjectNotFoundException> {
            collectionSubjects.getByIds(math.id, phantomSubject)
        }
    }
}
