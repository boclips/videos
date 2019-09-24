package com.boclips.videos.service.infrastructure

import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.users.client.model.contract.SelectedContentContract
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ApiUserContractServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `passes through client's response if all is well`() {
        whenever(userServiceClient.getContracts(anyString()))
            .thenReturn(listOf(testContract))

        assertThat(userContractService.getContracts("test-user")).containsOnly(testContract)
    }

    @Test
    fun `retries up to 3 times when client throws errors`() {
        whenever(userServiceClient.getContracts(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(listOf(testContract))

        assertThat(userContractService.getContracts("test-user")).containsOnly(testContract)
    }

    val testContract = SelectedContentContract().apply {
        name = "Test Contract"
        collectionIds = listOf("test-collection-id")
    }

    @Test
    fun `returns an empty list if it's not possible to retrieve contracts`() {
        whenever(userServiceClient.getContracts(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenThrow(RuntimeException("Something bad happened again!"))

        assertThat(userContractService.getContracts("test-user")).isEmpty()
    }

    @MockBean(name = "userServiceClient")
    override lateinit var userServiceClient: FakeUserServiceClient
}
