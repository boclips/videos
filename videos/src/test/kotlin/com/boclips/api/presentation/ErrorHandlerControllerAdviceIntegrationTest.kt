package com.boclips.api.presentation

import com.boclips.api.AbstractIntegrationTest
import com.boclips.api.PackageService
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

class ErrorHandlerControllerAdviceIntegrationTest : AbstractIntegrationTest() {

    @MockBean
    lateinit var packageService: PackageService

    @Test
    fun illegalFilterException_getsMappedTo400() {
        whenever(packageService.getById(any())).thenReturn(Mono.error(IllegalFilterException()))
        webClient.get().uri("/packages/1").exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun resourceNotFoundException_getsMappedTo404() {
        whenever(packageService.getById(any())).thenReturn(Mono.error(ResourceNotFoundException()))
        webClient.get().uri("/packages/1").exchange()
                .expectStatus().isNotFound
    }
}