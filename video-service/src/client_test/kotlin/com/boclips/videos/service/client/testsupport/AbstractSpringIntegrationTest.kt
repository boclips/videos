package com.boclips.videos.service.client.testsupport

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.VideoServiceApplication
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.jdbc.JdbcTestUtils
import java.time.Duration

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ContextConfiguration(classes = [VideoServiceApplication::class])
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test", "fakes", "fake-kaltura", "fake-search", "fake-youtube", "no-security")
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var repos: Set<MongoRepository<*, *>>

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var fakeSearchService: InMemorySearchService

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @BeforeEach
    fun resetState() {
        repos.forEach { it.deleteAll() }

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig")

        fakeSearchService.resetIndex()
        fakeKalturaClient.clear()
    }

    fun saveVideo(videoId: Long,
                  playbackId: PlaybackId = PlaybackId(type = KALTURA, value = "ref-id-$videoId"),
                  title: String = "Some title!",
                  description: String = "Some description!",
                  date: String = "2018-01-01",
                  duration: Duration = Duration.ofSeconds(10),
                  contentProvider: String = "AP",
                  contentProviderId: String = "provider-id-$videoId",
                  typeId: Int = 3,
                  keywords: List<String> = emptyList()
    ) {
        jdbcTemplate.update("""
            INSERT INTO metadata_orig (
                id,
                source,
                title,
                description,
                date,
                duration,
                reference_id,
                keywords,
                type_id,
                playback_id,
                playback_provider,
                unique_id,
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                videoId, contentProvider, title, description, date, "00:00:00", playbackId.value, keywords.joinToString(separator = ","), typeId, playbackId.value, playbackId.type.name, contentProviderId
        )

        fakeSearchService.upsert(sequenceOf(VideoMetadata(
                id = videoId.toString(),
                title = title,
                description = description,
                contentProvider = contentProvider,
                keywords = emptyList()
        )))

        when (playbackId.type) {
            KALTURA -> fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$videoId", referenceId = playbackId.value, duration = duration))
        }
    }


}
