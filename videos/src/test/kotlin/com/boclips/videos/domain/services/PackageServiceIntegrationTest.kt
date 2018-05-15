package com.boclips.videos.domain.services

import com.boclips.videos.testsupport.AbstractIntegrationTest
import com.boclips.videos.domain.model.Package
import com.boclips.videos.infrastructure.packages.PackageServiceImpl
import com.boclips.videos.testsupport.PEARSON_PACKAGE_ID
import com.boclips.videos.testsupport.PEARSON_PACKAGE_NAME
import com.boclips.videos.testsupport.SCHOOL_OF_LIFE_ID
import com.boclips.videos.testsupport.SKY_NEWS_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class PackageServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var packageService: PackageServiceImpl

    @Test
    fun getAll_returnsAllPackages() {
        val packages = packageService.getAll().collectList().block()

        assertThat(packages).hasSize(1)
        assertThat(packages!!.first().id).isEqualTo(PEARSON_PACKAGE_ID)
        assertThat(packages.first().name).isEqualTo(PEARSON_PACKAGE_NAME)
    }

    @Test
    fun getAll_fetchesContentProviders() {
        val packages = packageService.getAll().collectList().block()!!

        assertThat(packages.first().excludedContentProviders).hasSize(1)
        assertThat(packages.first().excludedContentProviders.first().id).isEqualTo(SKY_NEWS_ID)
    }

    @Test
    fun get_whenExists_returnsPackage() {
        val output: Package = packageService.getById(PEARSON_PACKAGE_ID).block()!!

        assertThat(output.id).isEqualTo(PEARSON_PACKAGE_ID)
        assertThat(output.name).isEqualTo(PEARSON_PACKAGE_NAME)
    }

    @Test
    fun get_fetchesContentProviders() {
        val output: Package = packageService.getById(PEARSON_PACKAGE_ID).block()!!

        assertThat(output.excludedContentProviders).hasSize(1)
        assertThat(output.excludedContentProviders.first().id).isEqualTo(SKY_NEWS_ID)
    }

    @Test
    fun excludeContentProvider_appendsCP() {
        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()!!

        val updatedCP = packageService.getById(PEARSON_PACKAGE_ID).block()!!

        assertThat(updatedCP.excludedContentProviders.map { it.id }).contains(SKY_NEWS_ID, SCHOOL_OF_LIFE_ID)
    }

}