package com.boclips.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class SourcesServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var sourcesService: SourcesService

    @Test
    fun getAllSources_returnsAllSources() {
        val sources = sourcesService.getAllSources()

        assertThat(sources).containsExactly(Source(name = "Sky News", dateCreated = "2017-05-13T15:56:44.822Z", dateUpdated = "2017-05-13T15:56:44.822Z", uuid = "86360cc5-019e-4b13-9048-1d571e825108"))
    }

    @Test
    fun createSource_whenDoesNotExist_createsNewSource() {
        val created = sourcesService.createSource("TeD")

        assertThat(created).isTrue()
        val source = sourcesService.getAllSources().find { it.name == "TeD" }!!
        assertThat(source.id).matches("[a-z0-9]{24}")
        assertThat(source.uuid).matches("[a-z0-9\\-]{36}")
        assertThat(source.dateCreated).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")
        assertThat(source.dateUpdated).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")
    }

    @Test
    fun createSource_whenExists_doesNotCreateANewOne() {
        val created = sourcesService.createSource("Sky News")

        assertThat(created).isFalse()
        assertThat(sourcesService.getAllSources()).hasSize(1)
    }
}