package com.boclips.api

import com.boclips.api.contentproviders.ContentProviderService
import com.boclips.api.testsupport.SKY_NEWS_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ContentProviderServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var contentProviderService: ContentProviderService

    @Test
    fun getAll_returnsAllContentProviders() {
        val sources = contentProviderService.getAll().collectList().block()

        assertThat(sources).hasSize(1)
        assertThat(sources!!.first().id).isEqualTo(SKY_NEWS_ID)
    }

    @Test
    fun create_whenDoesNotExist_createsNewContentProvider() {
        val created = contentProviderService.create("TeD").block()

        assertThat(created).isTrue()
        val source = contentProviderService.getAll().collectList().block()!!.find { it.name == "TeD" }!!
        assertThat(source.id).matches("[a-z0-9]{24}")
        assertThat(source.uuid).matches("[a-z0-9\\-]{36}")
        assertThat(source.dateCreated).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")
        assertThat(source.dateUpdated).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")
    }

    @Test
    fun create_whenExists_doesNotCreateANewOne() {
        val created = contentProviderService.create("Sky News").block()

        assertThat(created).isFalse()
        assertThat(contentProviderService.getAll().collectList().block()).hasSize(1)
    }

    @Test
    fun getById_whenExists_returnsContentProvider() {
        val contentProvider = contentProviderService.getById(SKY_NEWS_ID).block()!!

        assertThat(contentProvider.id).isEqualTo(SKY_NEWS_ID)
    }
}