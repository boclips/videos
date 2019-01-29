package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.nhaarman.mockito_kotlin.reset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.jdbc.JdbcTestUtils
import java.time.Duration
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fakes", "fake-kaltura", "fake-search", "fake-youtube", "fake-security")
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var repos: Set<MongoRepository<*, *>>

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var fakeSearchService: InMemorySearchService

    @Autowired
    lateinit var legacySearchService: LegacySearchService

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Autowired
    lateinit var fakeYoutubePlaybackProvider: TestYoutubePlaybackProvider

    @Autowired
    lateinit var kalturaPlaybackProvider: KalturaPlaybackProvider

    @Autowired
    lateinit var eventService: EventService

    @Autowired
    lateinit var createVideo: CreateVideo

    @BeforeEach
    fun resetState() {
        repos.forEach { it.deleteAll() }

        JdbcTestUtils.deleteFromTables(jdbcTemplate, "metadata_orig", "collection_video", "collection", "video_subject")

        fakeSearchService.safeRebuildIndex(emptySequence())
        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()

        reset(legacySearchService)
    }

    fun saveVideo(playbackId: PlaybackId = PlaybackId(type = KALTURA, value = "ref-id-${UUID.randomUUID()}"),
                  title: String = "Some title!",
                  description: String = "Some description!",
                  date: String = "2018-01-01",
                  duration: Duration = Duration.ofSeconds(10),
                  contentProvider: String = "AP",
                  contentProviderId: String = "content-partner-video-id-${playbackId.value}",
                  typeId: Int = 3,
                  keywords: List<String> = emptyList(),
                  subjects: Set<String> = emptySet()
    ): AssetId {
        when (playbackId.type) {
            KALTURA -> fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-${playbackId.value}", referenceId = playbackId.value, duration = duration))
            YOUTUBE -> fakeYoutubePlaybackProvider.addVideo(playbackId.value, "https://youtube.com/thumb/${playbackId.value}.png", duration = duration)
        }

        val id = createVideo.execute(CreateVideoRequest(
                provider = contentProvider,
                providerVideoId = contentProviderId,
                title = title,
                description = description,
                releasedOn = LocalDate.parse(date),
                duration = duration,
                legalRestrictions = "",
                keywords = keywords,
                videoType = LegacyVideoType.fromId(typeId).name,
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                subjects = subjects
        )).id

        return AssetId(id!!)
    }


}
