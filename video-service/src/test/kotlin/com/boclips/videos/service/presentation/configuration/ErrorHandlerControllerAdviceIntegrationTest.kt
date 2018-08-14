package com.boclips.videos.service.presentation.configuration

import com.boclips.videos.service.domain.services.PackageService
import com.boclips.videos.service.testsupport.AbstractIntegrationTest
import com.boclips.videos.service.presentation.IllegalFilterException
import com.boclips.videos.service.presentation.ResourceNotFoundException
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