package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.MetadataTestRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class DelegatingDuplicateServiceIntegrationTest: AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var metadataTestRepository: MetadataTestRepository

    @Autowired
    lateinit var subject: DelegatingDuplicateService

    @Test
    fun getDuplicateVideos() {
        metadataTestRepository.insert(id = "1", title = "great title", contentProvider = "Bloomie")
        metadataTestRepository.insert(id = "2", contentProviderId = "1", contentProvider = "cp")
        metadataTestRepository.insert(id = "3", contentProviderId = "1", contentProvider = "cp")

        val duplicates = subject.getDuplicates()

        assertThat(duplicates).hasSize(1)
        assertThat(duplicates.first().originalVideo.id).isEqualTo("2")
        assertThat(duplicates.first().duplicates.map { it.id }).containsExactly("3")
    }
}