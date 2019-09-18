package com.boclips.videos.service.infrastructure.legal.restrictions

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoLegalRestrictionsRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var repository: MongoLegalRestrictionsRepository

    @Test
    fun create() {
        val restrictions = repository.create("Do not use in the UK")

        assertThat(restrictions.text).isEqualTo("Do not use in the UK")
        assertThat(restrictions.id.value).isNotBlank()
    }

    @Test
    fun findById() {
        val id = repository.create("No restrictions").id

        val restrictions = repository.findById(id)

        assertThat(restrictions).isNotNull
        assertThat(restrictions?.id?.value).isNotBlank()
        assertThat(restrictions?.text).isEqualTo("No restrictions")
    }

    @Test
    fun findAll() {
        repository.create("Do not use in Spain")

        val allRestrictions = repository.findAll()

        assertThat(allRestrictions).hasSize(1)
        assertThat(allRestrictions.first().text).isEqualTo("Do not use in Spain")
        assertThat(allRestrictions.first().id.value).isNotBlank()
    }
}