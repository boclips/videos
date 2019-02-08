package com.boclips.videos.service.client.testsupport

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.VideoServiceApplication
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.mongodb.MongoClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ContextConfiguration(classes = [VideoServiceApplication::class])
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test", "fakes", "fake-kaltura", "fake-search", "fake-youtube", "no-security")
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var fakeSearchService: InMemorySearchService

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Autowired
    lateinit var fakeYoutubePlaybackProvider: TestYoutubePlaybackProvider

    @Autowired
    lateinit var mongoClient: MongoClient

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
        fakeKalturaClient.clear()
    }

    fun saveVideo(
        videoId: Long,
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
        fakeSearchService.upsert(
            sequenceOf(
                VideoMetadata(
                    id = videoId.toString(),
                    title = title,
                    description = description,
                    contentProvider = contentProvider,
                    keywords = emptyList(),
                    tags = listOf("classroom")
                )
            )
        )

        when (playbackId.type) {
            KALTURA -> fakeKalturaClient.addMediaEntry(
                createMediaEntry(
                    id = "entry-$videoId",
                    referenceId = playbackId.value,
                    duration = duration
                )
            )
            else -> {
            }
        }
    }
}
