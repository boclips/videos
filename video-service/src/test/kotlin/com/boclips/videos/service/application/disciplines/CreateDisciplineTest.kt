package com.boclips.videos.service.application.disciplines

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateDisciplineTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var createDiscipline: CreateDiscipline

    @Test
    fun `create discipline`() {
        val discipline = createDiscipline.invoke(CreateDisciplineRequest(name = "STEM", code = "code-123"))

        assertThat(discipline.id.value).isNotNull()
        assertThat(discipline.name).isEqualTo("STEM")
        assertThat(discipline.code).isEqualTo("code-123")
        assertThat(discipline.subjects).isEmpty()
    }
}