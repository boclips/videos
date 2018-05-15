package com.boclips.api.presentation

import com.boclips.api.domain.model.Package
import com.boclips.api.domain.services.PackageService
import com.boclips.api.infrastructure.packages.PackageServiceImpl
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RunWith(MockitoJUnitRunner::class)
class PackageControllerTest {

    @Mock
    lateinit var packageService: PackageService

    @InjectMocks
    lateinit var controller: PackageController

    @Test
    fun getPackages() {
        whenever(packageService.getAll()).thenReturn(Flux.just(Package(name = "bla", id = "package-id")))

        val output = controller.getPackages(UriComponentsBuilder.newInstance()).block()!!.content

        assertThat(output).hasSize(1)
        assertThat(output.first().name).isNotBlank()
        assertThat(output.first().getLink("self")).isNotNull
        assertThat(output.first().getLink("excludedContentProvider")).isNotNull
    }

    @Test
    fun getPackage() {
        whenever(packageService.getById("package-id")).thenReturn(Mono.just(Package(name = "bla", id = "package-id")))

        val output = controller.getPackage("package-id", UriComponentsBuilder.newInstance()).block()!!

        assertThat(output.name).isNotBlank()
        assertThat(output.getLink("self")).isNotNull
        assertThat(output.getLink("excludedContentProvider")).isNotNull
    }

    @Test(expected = ResourceNotFoundException::class)
    fun getPackage_whenNoResource_throwsResourceNotFoundException() {
        whenever(packageService.getById(any())).thenReturn(Mono.empty())
        controller.getPackage("package-id", UriComponentsBuilder.newInstance()).block()
    }

    @Test
    fun excludeContentProvider() {
        controller.excludeContentProvider("http://host:port/crap/content-providers/content-provider-id", "package-id")

        verify(packageService).excludeContentProvider("package-id", "content-provider-id")
    }

}