package com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions

import com.boclips.contentpartner.service.domain.model.contract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.infrastructure.contract.legalrestrictions.MongoContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoContractLegalRestrictionsRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var repository: ContractLegalRestrictionsRepository

    @Test
    fun `can find all legal restrictions`() {
        val restrictionOne = repository.create(
            text = "restriction one"
        )

        val restrictionTwo = repository.create(
            text = "restriction one"
        )

        val allRestrictions = repository.findAll()

        assertThat(allRestrictions).hasSize(2)
        assertThat(allRestrictions).extracting("id").containsExactlyInAnyOrder(restrictionOne.id, restrictionTwo.id)
    }
}