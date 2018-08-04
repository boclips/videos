package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.MetadataTestRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.findAll
import org.springframework.test.context.jdbc.Sql

class DelegatingDuplicateVideoServiceIntegrationTest : AbstractSpringIntegrationTest() {

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
        assertThat(duplicates.first().originalVideo.id).isEqualTo(2)
        assertThat(duplicates.first().duplicates.map { it.id }).containsExactly(3)
    }


    @Test
    @Sql(scripts = ["/db/migration/h2/duplicates.sql"])
    fun `delete duplicates and remaps baskets and playlists`() {
        assertThat(jdbcTemplate.queryForList("select id from metadata_orig where id=2516942 or id=2439228").map { it["id"] })
                .containsExactlyInAnyOrder(2439228, 2516942)

        subject.deleteDuplicates(subject.getDuplicates())

        assertThat(jdbcTemplate.queryForList("select id from metadata_orig where id=2516942 or id=2439228").map { it["id"] })
                .containsExactly(2439228)
        val baskets = mongoTemplate.findAll<Map<String, Any>>("orderlines")
        assertThat(baskets.map { it["asset_id"] }).containsExactly(2439228, 2439228)
        val videodescriptors = mongoTemplate.findAll<Map<String, Any>>("videodescriptors")
        assertThat(videodescriptors.map { it["reference_id"] }).containsExactly(2439228, 2439228)
        assertThat(videodescriptors.map { (it["connection"] as Map<String, Any>)["item"] })
                .containsExactlyInAnyOrder("5ad4a3e3e1ce3071d71edc1c", "5ad4a3e3e1ce3071d71edc1a")
    }
}