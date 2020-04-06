package com.boclips.contentpartner.service.infrastructure.newlegalrestriction

import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoNewLegalRestrictionsRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var repository: MongoNewLegalRestrictionsRepository

    @Test
    fun findAll() {
        repository.create(
            id = "ContentPartner",
            restrictions = listOf(
                SingleLegalRestriction(id = "idOne", text = "123"),
                SingleLegalRestriction(id = "idTwo", text = "678")
            )
        )

        repository.create(
            id = "ContentPartnerContracts",
            restrictions = listOf(
                SingleLegalRestriction(id = "idOne", text = "123"),
                SingleLegalRestriction(id = "idTwo", text = "678")
            )
        )

        val allRestrictions = repository.findAll()

        Assertions.assertThat(allRestrictions).hasSize(2)
        Assertions.assertThat(allRestrictions.first().id).isEqualTo("ContentPartner")
    }

    @Test
    fun findByRestrictionType() {
        repository.create(
            id = "ContentPartner",
            restrictions = listOf(
                SingleLegalRestriction(id = "idOne", text = "123"),
                SingleLegalRestriction(id = "idTwo", text = "678")
            )
        )

        repository.create(
            id = "ContentPartnerContracts",
            restrictions = listOf(
                SingleLegalRestriction(id = "idOne", text = "123"),
                SingleLegalRestriction(id = "idTwo", text = "678")
            )
        )

        val singleRestriction = repository.findOne("ContentPartner")

        Assertions.assertThat(singleRestriction?.id).isEqualTo("ContentPartner")
    }
}