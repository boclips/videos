package com.boclips.api.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PackageEntityTest {

    @Test
    fun toPackage_transformsId() {
        val transformedPackage = PackageEntity("the-id", "some-package", listOf()).toPackage()

        assertThat(transformedPackage.id).isEqualTo("the-id")
    }

    @Test
    fun toPackage_transformsName() {
        val transformedPackage = PackageEntity("the-id", "some-package", listOf()).toPackage()

        assertThat(transformedPackage.name).isEqualTo("some-package")
    }

    @Test
    fun toPackage_whenExcluded_transformsFilters() {
        val searchFilters = listOf(SearchFilter(SearchFilterType.Source, true, listOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders).containsExactlyInAnyOrder("item-1", "item-2")
    }

    @Test
    fun toPackage_whenIncluded_transformsFilters() {
        val searchFilters = listOf(SearchFilter(SearchFilterType.Source, false, listOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders).isEmpty()
    }

    @Test
    fun toPackage_whenFilterTypeNotSupported_filterIsExcluded() {
        val searchFilters = listOf(SearchFilter(SearchFilterType.Assettype, true, listOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders).isEmpty()
    }
}