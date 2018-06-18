package com.boclips.testsupport

import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.jdbc.JdbcTestUtils


@RunWith(SpringRunner::class)
@SpringBootTest
abstract class AbstractSpringIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")
    }
}