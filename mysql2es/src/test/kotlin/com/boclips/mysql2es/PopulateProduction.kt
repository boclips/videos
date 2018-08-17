package com.boclips.mysql2es

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("production")
class PopulateProduction {

    @Autowired
    lateinit var migrationService: MigrationService

    @Test
    fun addAllVideos() {
        migrationService.migrateData("select * from metadata_orig", "videos")
    }
}
