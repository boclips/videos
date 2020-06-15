package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetChannelContractsTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getContracts: GetContracts

    @Test
    fun `can fetch all contracts`() {
        val contractId = saveContract()
        val allContracts = getContracts(page = null, size = null)

        assertThat(allContracts.elements.map { it.id }).containsExactly(contractId.id)
    }

    @Test
    fun `can page contracts`() {
        saveContract(name = "a")
        saveContract(name = "b")

        val allContracts = getContracts(page = 0, size = 1)

        assertThat(allContracts.elements).hasSize(1)
        assertThat(allContracts.pageInfo.totalElements).isEqualTo(2)
        assertThat(allContracts.pageInfo.hasMoreElements).isTrue()
        assertThat(allContracts.pageInfo.pageRequest.size).isEqualTo(1)
        assertThat(allContracts.pageInfo.pageRequest.page).isEqualTo(0)
    }
}
