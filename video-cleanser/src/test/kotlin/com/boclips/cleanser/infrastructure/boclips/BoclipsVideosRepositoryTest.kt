package com.boclips.cleanser.infrastructure.boclips

import com.boclips.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class BoclipsVideosRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var boclipsVideosRepository: BoclipsVideosRepository

    @Test
    fun getAllIds() {
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(1, null)")
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(2, null)")

        assertThat(boclipsVideosRepository.getAllLegacyIds()).containsExactly(1, 2)
    }

    @Test
    fun getAllIds_withoutVideosWithReferenceId() {
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(1, 'a reference id')")
        jdbcTemplate.update("INSERT INTO metadata_orig(id) VALUES(2)")

        assertThat(boclipsVideosRepository.getAllLegacyIds()).containsExactly(2)
    }
}