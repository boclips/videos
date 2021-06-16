package com.boclips.videos.service.infrastructure.contentwarning

import com.boclips.videos.service.domain.model.contentwarning.ContentWarningId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class MongoContentWarningRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoContentWarningRepository: MongoContentWarningRepository

    @Test
    fun `find existing warning`() {
        val warning = mongoContentWarningRepository.create("test")

        val foundWarning = mongoContentWarningRepository.findById(warning.id)

        assertThat(foundWarning).isNotNull
        assertThat(foundWarning!!.label).isEqualTo("test")
    }

    @Test
    fun `return null if warning does not exist`() {

        val foundWarning = mongoContentWarningRepository.findById(ContentWarningId(ObjectId().toHexString()))
        assertThat(foundWarning).isNull()
    }

    @Test
    fun findAll() {
        mongoContentWarningRepository.create("test")
        mongoContentWarningRepository.create("test2")

        val warnings = mongoContentWarningRepository.findAll()
        assertThat(warnings.size).isEqualTo(2)
        assertThat(warnings[0].id).isNotNull
        assertThat(warnings[0].label).isEqualTo("test")
        assertThat(warnings[1].label).isEqualTo("test2")
    }

    @Test
    fun create() {
        val warning = mongoContentWarningRepository.create("test")
        val warnings = mongoContentWarningRepository.findAll()

        assertThat(warnings).containsOnly(warning)
    }
}
