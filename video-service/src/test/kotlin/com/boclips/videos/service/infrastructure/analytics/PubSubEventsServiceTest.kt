package com.boclips.videos.service.infrastructure.analytics

import com.boclips.eventbus.events.collection.CollectionAgeRangeChanged
import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionSubjectsChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
        eventService.saveSearchEvent(
            query = "the query",
            pageIndex = 4,
            pageSize = 2,
            totalResults = 20,
            pageVideoIds = listOf("v123")
        )

        val event = fakeEventBus.getEventOfType(VideosSearched::class.java)

        assertThat(event.query).isEqualTo("the query")
        assertThat(event.pageIndex).isEqualTo(4)
        assertThat(event.pageSize).isEqualTo(2)
        assertThat(event.totalResults).isEqualTo(20)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.pageVideoIds).containsExactly("v123")
    }

    @Test
    fun addVideoToCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.AddVideoToCollection(collectionId = CollectionId(collectionId), videoId = VideoId(videoId))
            )
        )

        val event = fakeEventBus.getEventOfType(VideoAddedToCollection::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun `isBoclipsEmployee when user is an employee`() {
        setSecurityContext(username = "david@boclips.com")

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(aValidId()), updateCommands = listOf(
                CollectionUpdateCommand.AddVideoToCollection(collectionId = CollectionId(aValidId()), videoId = VideoId(aValidId()))
            )
        )

        val event = fakeEventBus.getEventOfType(VideoAddedToCollection::class.java)
        assertThat(event.user.isBoclipsEmployee).isTrue()
    }

    @Test
    fun removeVideoFromCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.RemoveVideoFromCollection(collectionId = CollectionId(collectionId), videoId = VideoId(videoId))
            )
        )

        val event = fakeEventBus.getEventOfType(VideoRemovedFromCollection::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
    }

    @Test
    fun renameCollection() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.RenameCollection(collectionId = CollectionId(collectionId), title = "the new title")
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionRenamed::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.collectionTitle).isEqualTo("the new title")
    }

    @Test
    fun collectionMadePublic() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ChangeVisibility(collectionId = CollectionId(collectionId), isPublic = true)
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.isPublic).isTrue()
    }

    @Test
    fun collectionMadePrivate() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(aValidId()), updateCommands = listOf(
                CollectionUpdateCommand.ChangeVisibility(collectionId = CollectionId(collectionId), isPublic = false)
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.isPublic).isFalse()
    }

    @Test
    fun collectionSubjectsChanged() {
        val collectionId = aValidId()
        val aSubject = TestFactories.createSubject()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ReplaceSubjects(
                    collectionId = CollectionId(collectionId),
                    subjects = setOf(
                        aSubject
                    )
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionSubjectsChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.subjects).containsExactly(aSubject.id.value)
    }

    @Test
    fun collectionAgeRangeChanged() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ChangeAgeRange(collectionId = CollectionId(collectionId), minAge = 5, maxAge = 9)
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionAgeRangeChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.rangeMin).isEqualTo(5)
        assertThat(event.rangeMax).isEqualTo(9)
    }

    @Test
    fun `collectionAgeRangeChanged when no max bound`() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            collectionId = CollectionId(collectionId), updateCommands = listOf(
                CollectionUpdateCommand.ChangeAgeRange(collectionId = CollectionId(collectionId), minAge = 5, maxAge = null)
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionAgeRangeChanged::class.java)

        assertThat(event.rangeMax as Any?).isNull()
    }

    @Test
    fun saveBookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveBookmarkCollectionEvent(
            collectionId = CollectionId(collectionId)
        )

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.isBookmarked).isTrue()
    }

    @Test
    fun saveUnbookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveUnbookmarkCollectionEvent(
            collectionId = CollectionId(collectionId)
        )

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.isBookmarked).isFalse()
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
            playbackDevice = "device-id"
        )

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.videoIndex).isEqualTo(2)
        assertThat(event.playerId).isEqualTo("player-id")
        assertThat(event.segmentStartSeconds).isEqualTo(123)
        assertThat(event.segmentEndSeconds).isEqualTo(345)
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.playbackDevice).isEqualTo("device-id")
    }

    @Test
    fun savePlayerInteractedWithEvent() {
        val videoId = aValidId()
        eventService.savePlayerInteractedWithEvent(
            videoId = VideoId(videoId),
            playerId = "player-id",
            currentTime = 34,
            subtype = "captions-on",
            payload = mapOf<String, Any>(
                Pair("kind", "caption-kind"),
                Pair("language", "caption-language"),
                Pair("id", "caption-id"),
                Pair("label", "caption-label")
            )
        )

        val event = fakeEventBus.getEventOfType(VideoPlayerInteractedWith::class.java)

        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.user.isBoclipsEmployee).isFalse()
        assertThat(event.playerId).isEqualTo("player-id")
        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.currentTime).isEqualTo(34L)
        assertThat(event.subtype).isEqualTo("captions-on")
        assertThat(event.payload.size).isGreaterThan(0)
        assertThat(event.payload["kind"]).isEqualTo("caption-kind")
        assertThat(event.payload["id"]).isEqualTo("caption-id")
        assertThat(event.payload["language"]).isEqualTo("caption-language")
        assertThat(event.payload["label"]).isEqualTo("caption-label")
    }

    @Test
    fun saveVideoInteractedWith() {
        val videoId = aValidId()
        eventService.publishVideoInteractedWithEvent(videoId = VideoId(videoId), subtype = "share-to-google-classroom")

        val event = fakeEventBus.getEventOfType(VideoInteractedWith::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.subtype).isEqualTo("share-to-google-classroom")
        assertThat(event.user.id).isEqualTo("user@example.com")
        assertThat(event.payload).isEmpty()
    }

    @Test
    fun `events have url when referer header is present`() {
        mockMvc.perform(
            get("/v1/videos?query=abc").asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://teachers.boclips.com/videos?q=abc")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        val event = fakeEventBus.getEventOfType(VideosSearched::class.java)

        assertThat(event.url).isEqualTo("https://teachers.boclips.com/videos?q=abc")
    }
}
