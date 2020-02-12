package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.EduAgeRangeNotFoundException
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetEduAgeRangeIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getEduAgeRange: GetEduAgeRange
    @Autowired
    lateinit var eduAgeRangeRepository: EduAgeRangeRepository

    @Test
    fun `throws an EduAgeRangeNotFoundException when given age range is not found`() {
        assertThrows<EduAgeRangeNotFoundException> { getEduAgeRange(EduAgeRangeId("this does not exist")) }
    }

    @Test
    fun `returns expected age range when it's found`() {
        val id = "test-age-range-id"
        val eduAgeRange = TestFactories.createEduAgeRange(id = id)

        eduAgeRangeRepository.create(eduAgeRange)

        assertThat(getEduAgeRange(EduAgeRangeId(id))).isEqualTo(eduAgeRange)
    }
}