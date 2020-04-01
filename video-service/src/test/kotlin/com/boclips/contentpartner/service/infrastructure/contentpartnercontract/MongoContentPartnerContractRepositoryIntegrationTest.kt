package com.boclips.contentpartner.service.infrastructure.contentpartnercontract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class FindAll {

        @Test
        fun `can find all contracts`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 10, page = 0))
            assertThat(retrievedContracts.elements.map { it.id }).containsExactlyInAnyOrder(*contracts.map { it.id }.toTypedArray())
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isFalse()
        }

        @Test
        fun `returns correct page information`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 1, page = 0))
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isTrue()
            assertThat(retrievedContracts.pageInfo.totalElements).isEqualTo(2)
            assertThat(retrievedContracts.pageInfo.pageRequest.page).isEqualTo(0)
            assertThat(retrievedContracts.pageInfo.pageRequest.size).isEqualTo(1)
        }

        @Test
        fun `fetching the last page returns the correct size`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 2, page = 1))
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isFalse()
            assertThat(retrievedContracts.pageInfo.totalElements).isEqualTo(3)
            assertThat(retrievedContracts.pageInfo.pageRequest.page).isEqualTo(1)
            assertThat(retrievedContracts.pageInfo.pageRequest.size).isEqualTo(1)
        }
    }
}
