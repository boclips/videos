package com.boclips.contentpartner.service.infrastructure.agerange

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoAgeRangeRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var ageRangeRepository: MongoAgeRangeRepository

    @Test
    fun `can create a new age range`() {
        val ageRange = ChannelFactory.createAgeRange()

        val createdAgeRange = ageRangeRepository.create(ageRange = ageRange)

        assertThat(createdAgeRange).isEqualTo(ageRange)
    }

    @Test
    fun `can find an age range by id`() {
        val ageRange = ChannelFactory.createAgeRange()
        ageRangeRepository.create(ageRange = ageRange)
        val retrievedAsset = ageRangeRepository.findById(ageRange.id)

        assertThat(retrievedAsset?.id?.value).isEqualTo(ageRange.id.value)
    }

    @Test
    fun `can find all age ranges`() {
        val ageRange1 = ChannelFactory.createAgeRange(id = "id1")
        val ageRange2 = ChannelFactory.createAgeRange(id = "id2")

        ageRangeRepository.create(ageRange = ageRange1)
        ageRangeRepository.create(ageRange = ageRange2)

        val retrievedAsset = ageRangeRepository.findAll()

        assertThat(retrievedAsset).hasSize(2)
    }
}
