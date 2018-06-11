package com.boclips.cleanser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils

class BoclipsVideosRepositoryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var boclipsVideosRepository: BoclipsVideosRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")
    }

    @Test
    fun getAllIds() {
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(1, null)")
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(2, null)")

        assertThat(boclipsVideosRepository.getAllIds()).containsExactly(1, 2)
    }

    @Test
    fun getAllIds_withoutVideosWithReferenceId() {
        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id) VALUES(1, 'a reference id')")
        jdbcTemplate.update("INSERT INTO metadata_orig(id) VALUES(2)")

        assertThat(boclipsVideosRepository.getAllIds()).containsExactly(2)
    }
}