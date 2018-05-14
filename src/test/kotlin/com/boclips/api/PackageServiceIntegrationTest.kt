package com.boclips.api

import com.boclips.api.testsupport.PEARSON_PACKAGE_ID
import com.boclips.api.testsupport.PEARSON_PACKAGE_NAME
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class PackageServiceIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    lateinit var packageService: PackageService

    @Test
    fun getAll_returnsAllPackages() {
        val packages = packageService.getAll().collectList().block()

        assertThat(packages).hasSize(1)
        assertThat(packages!!.first().id).isEqualTo(PEARSON_PACKAGE_ID)
        assertThat(packages.first().name).isEqualTo(PEARSON_PACKAGE_NAME)
        assertThat(packages.first().excludedContentProviders).hasSize(1)
    }

    @Test
    fun get_whenExists_returnsPackage() {
        val output : Package = packageService.getById(PEARSON_PACKAGE_ID).block()!!

        assertThat(output.id).isEqualTo(PEARSON_PACKAGE_ID)
        assertThat(output.name).isEqualTo(PEARSON_PACKAGE_NAME)
        assertThat(output.excludedContentProviders).hasSize(1)
    }
}