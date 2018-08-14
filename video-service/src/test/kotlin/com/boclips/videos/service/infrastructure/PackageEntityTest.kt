package com.boclips.videos.service.infrastructure

import com.boclips.videos.service.infrastructure.packages.PackageEntity
import com.boclips.videos.service.infrastructure.packages.SearchFilter
import com.boclips.videos.service.infrastructure.packages.SearchFilterType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PackageEntityTest {

    @Test
    fun toPackage_transformsId() {
        val transformedPackage = PackageEntity("the-id", "some-package", mutableListOf()).toPackage()

        assertThat(transformedPackage.id).isEqualTo("the-id")
    }

    @Test
    fun toPackage_transformsName() {
        val transformedPackage = PackageEntity("the-id", "some-package", mutableListOf()).toPackage()

        assertThat(transformedPackage.name).isEqualTo("some-package")
    }

    @Test
    fun toPackage_whenExcluded_transformsFilters() {
        val searchFilters = mutableListOf(SearchFilter(SearchFilterType.Source, true, mutableSetOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders.map { it.id }).containsExactlyInAnyOrder("item-1", "item-2")
    }

    @Test
    fun toPackage_whenIncluded_transformsFilters() {
        val searchFilters = mutableListOf(SearchFilter(SearchFilterType.Source, false, mutableSetOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders).isEmpty()
    }

    @Test
    fun toPackage_whenFilterTypeNotSupported_filterIsExcluded() {
        val searchFilters = mutableListOf(SearchFilter(SearchFilterType.Assettype, true, mutableSetOf("item-1", "item-2")))
        val transformedPackage = PackageEntity(
                id = "the-id",
                name = "some-package",
                searchFilters = searchFilters
        ).toPackage()

        assertThat(transformedPackage.excludedContentProviders).isEmpty()
    }
}