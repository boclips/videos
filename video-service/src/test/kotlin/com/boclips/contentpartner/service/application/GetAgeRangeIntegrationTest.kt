package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.AgeRangeNotFoundException
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetAgeRangeIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getAgeRange: GetAgeRange
    @Autowired
    lateinit var ageRangeRepository: AgeRangeRepository

    @Test
    fun `throws an not found exception when given age range is not found`() {
        assertThrows<AgeRangeNotFoundException> { getAgeRange(AgeRangeId("this does not exist")) }
    }

    @Test
    fun `returns expected age range when it's found`() {
        val id = "test-age-range-id"
        val ageRange = TestFactories.createAgeRange(id = id)

        ageRangeRepository.create(ageRange)

        assertThat(getAgeRange(AgeRangeId(id))).isEqualTo(ageRange)
    }
}
