package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoContentPartnerContractRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerContractRepository: ContentPartnerContractRepository

    @Test
    fun `can create a content partner contract`() {
        val original = ContentPartnerContractFactory.sample()
        val created = contentPartnerContractRepository.create(original)
        assertThat(original.id.value).isEqualTo(created.value)
    }

    @Test
    fun `can find a content partner contract`() {
        val original = ContentPartnerContractFactory.sample()
        contentPartnerContractRepository.create(original)
        val found = contentPartnerContractRepository.findById(original.id)
        assertThat(found).isEqualTo(original)
    }

    @Test
    fun `can find all contracts`() {
        val contracts = listOf(
            ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
            ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
        )

        contracts.map { contentPartnerContractRepository.create(it) }

        val retrievedContracts = contentPartnerContractRepository.findAll()
        assertThat(retrievedContracts.map { it.id }).containsExactlyInAnyOrder(*contracts.map { it.id }.toTypedArray())
    }
}
