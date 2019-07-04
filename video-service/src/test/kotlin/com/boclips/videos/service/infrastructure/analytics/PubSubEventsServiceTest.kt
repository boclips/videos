package com.boclips.videos.service.infrastructure.analytics

import com.boclips.events.types.collection.CollectionAgeRangeChanged
import com.boclips.events.types.collection.CollectionBookmarkChanged
import com.boclips.events.types.collection.CollectionRenamed
import com.boclips.events.types.collection.CollectionSubjectsChanged
import com.boclips.events.types.collection.CollectionVisibilityChanged
import com.boclips.events.types.collection.VideoAddedToCollection
import com.boclips.events.types.collection.VideoRemovedFromCollection
import com.boclips.events.types.video.VideoPlayerInteractedWith
import com.boclips.events.types.video.VideoSegmentPlayed
import com.boclips.events.types.video.VideosSearched
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.asTeacher
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.messaging.MessageChannel
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.reflect.KClass

class PubSubEventsServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventService: EventService

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        setSecurityContext(username = "user@example.com")
    }

    @Test
    fun saveSearchEvent() {
        eventService.saveSearchEvent(query = "the query", pageIndex = 4, pageSize = 2, totalResults = 20)

        val message = getMessage(topics.videosSearched(), VideosSearched::class)
        assertThat(message.query).isEqualTo("the query")
        assertThat(message.pageIndex).isEqualTo(4)
        assertThat(message.pageSize).isEqualTo(2)
        assertThat(message.totalResults).isEqualTo(20)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun addVideoToCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.AddVideoToCollection(videoId = VideoId(videoId))
            )
        )

        val message = getMessage(topics.videoAddedToCollection(), VideoAddedToCollection::class)
        assertThat(message.videoId).isEqualTo(videoId)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun `isBoclipsEmployee when user is an employee`() {
        setSecurityContext(username = "david@boclips.com")

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(aValidId()), updateCommands = listOf(
                CollectionUpdateCommand.AddVideoToCollection(videoId = VideoId(aValidId()))
            )
        )

        val message = getMessage(topics.videoAddedToCollection(), VideoAddedToCollection::class)
        assertThat(message.user.isBoclipsEmployee).isTrue()
    }

    @Test
    fun removeVideoFromCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.RemoveVideoFromCollection(videoId = VideoId(videoId))
            )
        )

        val message = getMessage(topics.videoRemovedFromCollection(), VideoRemovedFromCollection::class)
        assertThat(message.videoId).isEqualTo(videoId)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun renameCollection() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.RenameCollection(title = "the new title")
            )
        )

        val message = getMessage(topics.collectionRenamed(), CollectionRenamed::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.collectionTitle).isEqualTo("the new title")
    }

    @Test
    fun collectionMadePublic() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ChangeVisibility(isPublic = true)
            )
        )

        val message = getMessage(topics.collectionVisibilityChanged(), CollectionVisibilityChanged::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.isPublic).isTrue()
    }

    @Test
    fun collectionMadePrivate() {
        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(aValidId()), updateCommands = listOf(
                CollectionUpdateCommand.ChangeVisibility(isPublic = false)
            )
        )

        val message = getMessage(topics.collectionVisibilityChanged(), CollectionVisibilityChanged::class)
        assertThat(message.isPublic).isFalse()
    }

    @Test
    fun collectionSubjectsChanged() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ReplaceSubjects(
                    subjects = setOf(
                        SubjectId(
                            "subject-1"
                        )
                    )
                )
            )
        )

        val message = getMessage(topics.collectionSubjectsChanged(), CollectionSubjectsChanged::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.subjects).containsExactly("subject-1")
    }

    @Test
    fun collectionAgeRangeChanged() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ChangeAgeRange(minAge = 5, maxAge = 9)
            )
        )

        val message = getMessage(topics.collectionAgeRangeChanged(), CollectionAgeRangeChanged::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.rangeMin).isEqualTo(5)
        assertThat(message.rangeMax).isEqualTo(9)
    }

    @Test
    fun `collectionAgeRangeChanged when no max bound`() {
        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(aValidId()), updateCommands = listOf(
                CollectionUpdateCommand.ChangeAgeRange(minAge = 5, maxAge = null)
            )
        )

        val message = getMessage(topics.collectionAgeRangeChanged(), CollectionAgeRangeChanged::class)
        assertThat(message.rangeMax as Any?).isNull()
    }

    @Test
    fun saveBookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveBookmarkCollectionEvent(
            collectionId = CollectionId(collectionId)
        )

        val message = getMessage(topics.collectionBookmarkChanged(), CollectionBookmarkChanged::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.isBookmarked).isTrue()
    }

    @Test
    fun saveUnbookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveUnbookmarkCollectionEvent(
            collectionId = CollectionId(collectionId)
        )

        val message = getMessage(topics.collectionBookmarkChanged(), CollectionBookmarkChanged::class)
        assertThat(message.collectionId).isEqualTo(collectionId)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.isBookmarked).isFalse()
    }

    @Test
    fun savePlaybackEvent() {
        val videoId = aValidId()
        eventService.savePlaybackEvent(
            videoId = VideoId(videoId),
            videoIndex = 2,
            playerId = "player-id",
            segmentStartSeconds = 123,
            segmentEndSeconds = 345,
            videoDurationSeconds = 50
        )

        val message = getMessage(topics.videoSegmentPlayed(), VideoSegmentPlayed::class)
        assertThat(message.videoId).isEqualTo(videoId)
        assertThat(message.videoIndex).isEqualTo(2)
        assertThat(message.playerId).isEqualTo("player-id")
        assertThat(message.segmentStartSeconds).isEqualTo(123)
        assertThat(message.segmentEndSeconds).isEqualTo(345)
        assertThat(message.videoDurationSeconds).isEqualTo(50)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun savePlayerInteractedWithEvent() {
        val videoId = aValidId()
        eventService.savePlayerInteractedWithEvent(
            videoId = VideoId(videoId),
            playerId = "player-id",
            videoDurationSeconds = 50,
            currentTime = 34,
            subtype = "captions-on",
            payload = mapOf<String, Any>(
                Pair("kind", "caption-kind"),
                Pair("language", "caption-language"),
                Pair("id", "caption-id"),
                Pair("label", "caption-label")
            )
        )

        val message = getMessage(topics.videoPlayerInteractedWith(), VideoPlayerInteractedWith::class)
        assertThat(message.user.id).isEqualTo("user@example.com")
        assertThat(message.user.isBoclipsEmployee).isFalse()
        assertThat(message.playerId).isEqualTo("player-id")
        assertThat(message.videoId).isEqualTo(videoId)
        assertThat(message.videoDurationSeconds).isEqualTo(50)
        assertThat(message.currentTime).isEqualTo(34)
        assertThat(message.subtype).isEqualTo("captions-on")
        assertThat(message.payload.size).isGreaterThan(0)
        assertThat(message.payload["kind"]).isEqualTo("caption-kind")
        assertThat(message.payload["id"]).isEqualTo("caption-id")
        assertThat(message.payload["language"]).isEqualTo("caption-language")
        assertThat(message.payload["label"]).isEqualTo("caption-label")
    }

    @Test
    fun `events have url when referer header is present`() {
        mockMvc.perform(
            get("/v1/videos?query=abc").asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://teachers.boclips.com/videos?q=abc")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        val message = getMessage(topics.videosSearched(), VideosSearched::class)
        assertThat(message.url).isEqualTo("https://teachers.boclips.com/videos?q=abc")
    }

    private fun <T : Any> getMessage(topicChannel: MessageChannel, cls: KClass<T>): T {
        val message = messageCollector.forChannel(topicChannel).poll()
        assertThat(message).isNotNull
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(message.payload.toString(), cls.java)
    }
}
