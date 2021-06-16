package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.api.request.discipline.UpdateDisciplineRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class UpdateDisciplineTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var createDiscipline: CreateDiscipline

    @Autowired
    lateinit var updateDiscipline: UpdateDiscipline

    @Autowired
    lateinit var getDiscipline: GetDiscipline

    @Test
    fun `update a discipline with valid id`() {
        val discipline = createDiscipline(CreateDisciplineRequest(name = "Humanities", code = "humanities"))

        updateDiscipline(
            id = discipline.id.value,
            updateDisciplineRequest = UpdateDisciplineRequest(name = "Social Studies", code = "social-studies")
        )

        val updatedDiscipline = getDiscipline(disciplineId = discipline.id.value)

        assertThat(updatedDiscipline.name).isEqualTo("Social Studies")
        assertThat(updatedDiscipline.code).isEqualTo("social-studies")
    }

    @Test
    fun `attempt to update a discipline with invalid id`() {
        assertThrows<ResourceNotFoundApiException> {
            updateDiscipline(
                id = ObjectId().toHexString(),
                updateDisciplineRequest = UpdateDisciplineRequest(name = "Social Studies", code = "social-studies")
            )
        }
    }
}
