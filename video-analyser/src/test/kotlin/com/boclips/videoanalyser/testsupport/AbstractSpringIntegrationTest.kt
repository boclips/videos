package com.boclips.videoanalyser.testsupport

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
abstract class AbstractSpringIntegrationTest : AbstractWireMockTest() {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        jdbcTemplate.execute("""
            create table if not exists metadata_orig
                (
                      id             int auto_increment
                    primary key,
                  source         varchar(45)     null,
                  unique_id      mediumtext      null,
                  title          mediumtext      null,
                  description    mediumtext      null,
                  date           date            null,
                  duration       varchar(12)     null,
                  reference_id   varchar(45)     null
                );
        """.trimIndent())

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")
    }
}
