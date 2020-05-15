package com.boclips.videos.service.infrastructure.contentwarning

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired

internal class MongoContentWarningRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoContentWarningRepository: MongoContentWarningRepository

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