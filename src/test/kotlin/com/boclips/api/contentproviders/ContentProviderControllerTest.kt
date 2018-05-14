package com.boclips.api.contentproviders

import com.boclips.api.presentation.ResourceNotFoundException
import com.boclips.api.presentation.resources.ContentProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.hateoas.Link
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@RunWith(MockitoJUnitRunner::class)
class ContentProviderControllerTest {

    @Mock
    lateinit var contentProviderService: ContentProviderService

    @InjectMocks
    lateinit var controller: ContentProviderController

    @Test
    fun getContentProviderService() {
        whenever(contentProviderService.getById("content-provider-id")).thenReturn(Mono.just(com.boclips.api.contentproviders.ContentProvider("bla").apply { id = "content-provider-id" }))
        val expectedResource = ContentProvider(name = "bla").apply { add(Link("/content-providers/content-provider-id", "self")) }

        val output = controller.getContentProvider("content-provider-id", UriComponentsBuilder.newInstance()).block()!!

        assertThat(output.name).isEqualTo(expectedResource.name)
        assertThat(output.getLink("self")).isEqualTo(Link("/content-providers/content-provider-id"))
    }

    @Test(expected = ResourceNotFoundException::class)
    fun getContentProviderService_whenNoResource_throwsResourceNotFoundException() {
        whenever(contentProviderService.getById(any())).thenReturn(Mono.empty())
        controller.getContentProvider("content-provider-id", UriComponentsBuilder.newInstance()).block()
    }

}