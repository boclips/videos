package com.boclips.videos.domain.services

import com.boclips.videos.infrastructure.packages.PackageEntity
import com.boclips.videos.infrastructure.packages.PackageRepository
import com.boclips.videos.infrastructure.packages.SearchFilter
import com.boclips.videos.infrastructure.packages.SearchFilterType
import com.boclips.videos.infrastructure.packages.PackageServiceImpl
import com.boclips.videos.presentation.IllegalFilterException
import com.boclips.videos.testsupport.PEARSON_PACKAGE_ID
import com.boclips.videos.testsupport.SCHOOL_OF_LIFE_ID
import com.boclips.videos.testsupport.SKY_NEWS_ID
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.solidsoft.mockito.java8.AssertionMatcher.assertArg
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import reactor.core.publisher.Mono

@RunWith(MockitoJUnitRunner::class)
class PackageServiceImplTest {

    @InjectMocks
    lateinit var packageService: PackageServiceImpl

    @Mock
    lateinit var packageRepository: PackageRepository

    @Before
    fun setUp() {
        whenever(packageRepository.save(any<PackageEntity>())).thenReturn(Mono.empty())
    }

    @Test
    fun excludeContentProvider_whenOtherExcludedCPs_appendsCP() {
        whenever(packageRepository.findById(PEARSON_PACKAGE_ID)).thenReturn(Mono.just(
                PackageEntity(id = "irrelevant", name = "irrelevant", searchFilters = mutableListOf(
                        SearchFilter(_refType = SearchFilterType.Source, invertFilter = true, items = mutableSetOf(SKY_NEWS_ID))
                ))))

        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()

        verify(packageRepository).save(assertArg {
            assertThat(it.searchFilters.first().items).contains(SKY_NEWS_ID, SCHOOL_OF_LIFE_ID)
        })
    }

    @Test(expected = IllegalFilterException::class)
    fun excludeContentProvider_whenNoOtherExcludedCPs_appendsCP() {
        whenever(packageRepository.findById(PEARSON_PACKAGE_ID)).thenReturn(Mono.just(
                PackageEntity(id = "irrelevant", name = "irrelevant", searchFilters = mutableListOf(
                        SearchFilter(_refType = SearchFilterType.Source, invertFilter = false, items = mutableSetOf())
                ))))

        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()
    }

    @Test
    fun excludeContentProvider_whenOnlyAssetType_createsCPFilter() {
        whenever(packageRepository.findById(PEARSON_PACKAGE_ID)).thenReturn(Mono.just(
                PackageEntity(id = "irrelevant", name = "irrelevant", searchFilters = mutableListOf(
                        SearchFilter(_refType = SearchFilterType.Assettype, invertFilter = false, items = mutableSetOf())
                ))))

        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()

        verify(packageRepository).save(assertArg {
            assertThat(it.searchFilters.first({ it._refType == SearchFilterType.Source }).items).contains(SCHOOL_OF_LIFE_ID)
        })
    }

    @Test
    fun excludeContentProvider_whenContentProviderAddedTwice_createsNoDuplicates() {
        whenever(packageRepository.findById(PEARSON_PACKAGE_ID)).thenReturn(Mono.just(
                PackageEntity(id = "irrelevant", name = "irrelevant", searchFilters = mutableListOf(
                        SearchFilter(_refType = SearchFilterType.Assettype, invertFilter = false, items = mutableSetOf())
                ))))

        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()
        packageService.excludeContentProvider(PEARSON_PACKAGE_ID, SCHOOL_OF_LIFE_ID).block()

        verify(packageRepository, times(2)).save(assertArg {
            assertThat(it.searchFilters.first({ it._refType == SearchFilterType.Source }).items).hasSize(1)
        })
    }
}