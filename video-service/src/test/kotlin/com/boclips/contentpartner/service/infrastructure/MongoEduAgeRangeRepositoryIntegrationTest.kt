package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoEduAgeRangeRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var eduAgeRangeRepository: MongoEduAgeRangeRepository

    @Test
    fun `can create a new age range`() {
        val eduAgeRange = TestFactories.createEduAgeRange()

        val createdAgeRange = eduAgeRangeRepository.create(eduAgeRange = eduAgeRange)

        assertThat(createdAgeRange).isEqualTo(eduAgeRange)
    }

    @Test
    fun `can find an age range by id`() {
        val eduAgeRange = TestFactories.createEduAgeRange()
        eduAgeRangeRepository.create(eduAgeRange = eduAgeRange)
        val retrievedAsset = eduAgeRangeRepository.findById(eduAgeRange.id)

        assertThat(retrievedAsset?.id?.value).isEqualTo(eduAgeRange.id.value)
    }

    @Test
    fun `can find all age ranges`() {
        val eduAgeRange1 = TestFactories.createEduAgeRange(id = "id1")
        val eduAgeRange2 = TestFactories.createEduAgeRange(id = "id2")

        eduAgeRangeRepository.create(eduAgeRange = eduAgeRange1)
        eduAgeRangeRepository.create(eduAgeRange = eduAgeRange2)

        val retrievedAsset = eduAgeRangeRepository.findAll()

        assertThat(retrievedAsset).hasSize(2)
    }
}