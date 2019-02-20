package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.video.BulkUpdateVideo
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.boclips.videos.service.testsupport.fakes.FakeEventService
import com.mongodb.MongoClient
import com.nhaarman.mockito_kotlin.reset
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import mu.KLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fakes", "fake-kaltura", "fake-search", "fake-youtube", "fake-security", "fake-event-service")
abstract class AbstractSpringIntegrationTest {

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
    lateinit var eventService: FakeEventService

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var createCollection: CreateCollection

    @Autowired
    lateinit var bulkUpdateVideo: BulkUpdateVideo

    @Autowired
    lateinit var mongoClient: MongoClient

    companion object : KLogging() {
        private var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                val starter = MongodStarter.getDefaultInstance()
                val host = "localhost"
                val port = 27017

                logger.info { "Booting up MongoDB ${Version.Main.V3_6} on $host:$port" }

                val mongoConfig = MongodConfigBuilder()
                    .version(Version.Main.V3_6)
                    .net(Net(host, port, Network.localhostIsIPv6()))
                    .build()

                val mongoExecutable = starter.prepare(mongoConfig)
                mongoProcess = mongoExecutable.start()
            }
        }
    }

    @BeforeEach
    fun resetState() {
        mongoClient.apply {
            listDatabaseNames()
                .filterNot { setOf("admin", "config").contains(it) }
                .forEach {
                    println("Dropping $it")
                    dropDatabase(it)
                }
        }

        fakeSearchService.safeRebuildIndex(emptySequence())
        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()
        eventService.clear()

        reset(legacySearchService)
    }

    fun saveVideo(
        playbackId: PlaybackId = PlaybackId(type = KALTURA, value = "ref-id-${UUID.randomUUID()}"),
        title: String = "Some title!",
        description: String = "Some description!",
        date: String = "2018-01-01",
        duration: Duration = Duration.ofSeconds(10),
        contentProvider: String = "AP",
        contentProviderId: String = "content-partner-video-id-${playbackId.value}",
        typeId: Int = 3,
        keywords: List<String> = emptyList(),
        subjects: Set<String> = emptySet(),
        searchable: Boolean = true
    ): AssetId {
        when (playbackId.type) {
            KALTURA -> fakeKalturaClient.addMediaEntry(
                createMediaEntry(
                    id = "entry-${playbackId.value}",
                    referenceId = playbackId.value,
                    duration = duration
                )
            )
            YOUTUBE -> fakeYoutubePlaybackProvider.addVideo(
                playbackId.value,
                "https://youtube.com/thumb/${playbackId.value}.png",
                duration = duration
            )
        }

        val id = createVideo(
            CreateVideoRequest(
                provider = contentProvider,
                providerVideoId = contentProviderId,
                title = title,
                description = description,
                releasedOn = LocalDate.parse(date),
                legalRestrictions = "",
                keywords = keywords,
                videoType = LegacyVideoType.fromId(typeId).name,
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                subjects = subjects,
                searchable = searchable
            )
        ).id

        return AssetId(id!!)
    }

    fun changeVideoStatus(id: String, status: VideoResourceStatus) {
        bulkUpdateVideo(BulkUpdateRequest(listOf(id), status))
    }
}
