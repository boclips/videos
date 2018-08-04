package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.TestFactory.Companion.boclipsVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.findAll
import org.springframework.test.context.jdbc.Sql

class VideoRemapperMongoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var subject: VideoRemapperMongoRepository

    @Test
    @Sql(scripts = ["/db/migration/h2/duplicates.sql"])
    fun `remapper updates baskets`() {
        assertThat(mongoTemplate.findAll<Map<String, Any>>("orderlines").map { it["asset_id"] })
                .containsExactly(2516942, 2439228)

        subject.remapBasketsPlaylistsAndCollections(DuplicateVideo(
                originalVideo = boclipsVideo(id = 2439228),
                duplicates = listOf(boclipsVideo(id = 2516942)))
        )

        val baskets = mongoTemplate.findAll<Map<String, Any>>("orderlines")
        assertThat(baskets.map { it["asset_id"] }).containsExactly(2439228, 2439228)
    }

    @Test
    @Sql(scripts = ["/db/migration/h2/duplicates.sql"])
    fun `remapper updates playlists`() {
        assertThat(mongoTemplate.findAll<Map<String, Any>>("videodescriptors").map { it["reference_id"] })
                .containsExactly(2516942, 2439228)

        subject.remapBasketsPlaylistsAndCollections(DuplicateVideo(
                originalVideo = boclipsVideo(id = 2439228),
                duplicates = listOf(boclipsVideo(id = 2516942)))
        )

        val videodescriptors = mongoTemplate.findAll<Map<String, Any>>("videodescriptors")
        assertThat(videodescriptors.map { it["reference_id"] }).containsExactly(2439228, 2439228)
    }
}