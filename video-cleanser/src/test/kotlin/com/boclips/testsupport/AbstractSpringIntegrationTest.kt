package com.boclips.testsupport

import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.jdbc.JdbcTestUtils


@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractSpringIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS metadata_orig
                (
                    id INT auto_increment
                        PRIMARY KEY,
                    reference_id VARCHAR(666)
                );
        """.trimIndent())

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")
    }
}