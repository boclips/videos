package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.testsupport.fakes.FakeSearchService
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-kaltura", "fake-search", "fake-requestid")
@Transactional
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var repos: Set<MongoRepository<*, *>>

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var fakeSearchService: FakeSearchService

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Before
    fun resetState() {
        repos.forEach { it.deleteAll() }

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")

        fakeSearchService.reset()

        fakeKalturaClient.addMediaEntry(mediaEntry("1"))
        fakeKalturaClient.addMediaEntry(mediaEntry("2"))
        fakeKalturaClient.addMediaEntry(mediaEntry("3"))
        fakeKalturaClient.addMediaEntry(mediaEntry("4"))
        fakeKalturaClient.addMediaEntry(mediaEntry("5"))
    }

    fun saveVideo(videoId: Long,
                  title: String = "Some title!",
                  description: String = "Some description!",
                  date: String = "2018-01-01",
                  duration: String = "00:10:00",
                  contentProvider: String = "AP",
                  referenceId: String = "ref-id-1") {
        jdbcTemplate.update("""
            INSERT INTO metadata_orig (
                id,
                source,
                title,
                description,
                date,
                duration,
                reference_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
                videoId, contentProvider, title, description, date, duration, referenceId
        )
    }

    fun mediaEntry(id: String): MediaEntry? {
        return MediaEntry.builder()
                .id(id)
                .referenceId("ref-id-$id")
                .streams(StreamUrls("https://stream/[FORMAT]/video-$id.mp4"))
                .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
                .duration(Duration.ofMinutes(1))
                .build()
    }
}
