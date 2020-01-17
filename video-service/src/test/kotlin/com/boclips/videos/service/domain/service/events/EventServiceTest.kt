package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.events.collection.CollectionAgeRangeChanged
import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.eventbus.events.collection.CollectionInteractedWith
import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionSubjectsChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.eventbus.events.video.VideoInteractedWith
import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.TestFactories.createCollectionUpdateResult
import com.boclips.videos.service.testsupport.UserFactory
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZonedDateTime

class EventServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventService: EventService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun saveSearchEvent() {
        eventService.saveSearchEvent(
            query = "the query",
            pageIndex = 4,
            pageSize = 2,
            totalResults = 20,
            pageVideoIds = listOf("v123"),
            user = UserFactory.sample(id = "user@example.com")
        )

        val event = fakeEventBus.getEventOfType(VideosSearched::class.java)

        assertThat(event.query).isEqualTo("the query")
        assertThat(event.pageIndex).isEqualTo(4)
        assertThat(event.pageSize).isEqualTo(2)
        assertThat(event.totalResults).isEqualTo(20)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.pageVideoIds).containsExactly("v123")
    }

    @Test
    fun addVideoToCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.AddVideoToCollection(
                    collectionId = CollectionId(collectionId),
                    videoId = VideoId(videoId),
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(VideoAddedToCollection::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
    }

    @Test
    fun removeVideoFromCollection() {
        val collectionId = aValidId()
        val videoId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.RemoveVideoFromCollection(
                    collectionId = CollectionId(collectionId),
                    videoId = VideoId(videoId),
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(VideoRemovedFromCollection::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
    }

    @Test
    fun renameCollection() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.RenameCollection(
                    collectionId = CollectionId(collectionId),
                    title = "the new title",
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionRenamed::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.collectionTitle).isEqualTo("the new title")
    }

    @Test
    fun collectionMadePublic() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.ChangeVisibility(
                    collectionId = CollectionId(collectionId),
                    isPublic = true,
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.isPublic).isTrue()
    }

    @Test
    fun collectionMadePrivate() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.ChangeVisibility(
                    collectionId = CollectionId(collectionId),
                    isPublic = false,
                    user = UserFactory.sample()
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.isPublic).isFalse()
    }

    @Test
    fun collectionSubjectsReplaced() {
        val collectionId = aValidId()
        val subject = TestFactories.createSubject()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                collection = TestFactories.createCollection(
                    subjects = setOf(subject)
                ),
                command = CollectionUpdateCommand.ReplaceSubjects(
                    collectionId = CollectionId(collectionId),
                    subjects = setOf(subject),
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionSubjectsChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.subjects).containsExactly(subject.id.value)
    }

    @Test
    fun collectionSubjectRemoved() {
        val collectionId = aValidId()
        val removedSubject = TestFactories.createSubject(name = "physics")
        val anotherSubject = TestFactories.createSubject(name = "maths")

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                collection = TestFactories.createCollection(subjects = setOf(anotherSubject)),
                command = CollectionUpdateCommand.RemoveSubjectFromCollection(
                    collectionId = CollectionId(collectionId),
                    subjectId = removedSubject.id,
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionSubjectsChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.subjects).containsExactly(anotherSubject.id.value)
    }

    @Test
    fun collectionAgeRangeChanged() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.ChangeAgeRange(
                    collectionId = CollectionId(collectionId),
                    minAge = 5,
                    maxAge = 9,
                    user = UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionAgeRangeChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.rangeMin).isEqualTo(5)
        assertThat(event.rangeMax).isEqualTo(9)
    }

    @Test
    fun `collectionAgeRangeChanged when no max bound`() {
        val collectionId = aValidId()

        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.ChangeAgeRange(
                    collectionId = CollectionId(collectionId),
                    minAge = 5,
                    maxAge = null,
                    user = UserFactory.sample()
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionAgeRangeChanged::class.java)

        assertThat(event.rangeMax as Any?).isNull()
    }

    @Test
    fun saveBookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.Bookmark(
                    CollectionId(collectionId),
                    UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.isBookmarked).isTrue()
    }

    @Test
    fun saveUnbookmarkCollectionEvent() {
        val collectionId = aValidId()
        eventService.saveUpdateCollectionEvent(
            createCollectionUpdateResult(
                command = CollectionUpdateCommand.Unbookmark(
                    CollectionId(collectionId),
                    UserFactory.sample(id = "user@example.com")
                )
            )
        )

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.isBookmarked).isFalse()
    }

    @Test
    fun saveCollectionInteractedWithEvent() {
        val collectionId = aValidId()
        eventService.saveCollectionInteractedWithEvent(
            collectionId,
            CollectionInteractionType.NAVIGATE_TO_COLLECTION_DETAILS,
            UserFactory.sample(id = "user@example.com")
        )

        val event = fakeEventBus.getEventOfType(CollectionInteractedWith::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.subtype).isEqualTo(CollectionInteractionType.NAVIGATE_TO_COLLECTION_DETAILS)
    }

    @Test
    fun savePlaybackEvent() {
        val videoId = aValidId()
        eventService.savePlaybackEvent(
            videoId = VideoId(videoId),
            videoIndex = 2,
            segmentStartSeconds = 123,
            segmentEndSeconds = 345,
            playbackDevice = "device-id",
            user = UserFactory.sample(id = "user@example.com"),
            timestamp = ZonedDateTime.now()
        )

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.videoIndex).isEqualTo(2)
        assertThat(event.segmentStartSeconds).isEqualTo(123)
        assertThat(event.segmentEndSeconds).isEqualTo(345)
        assertThat(event.userId).isEqualTo("user@example.com")
        assertThat(event.playbackDevice).isEqualTo("device-id")
    }

    @Test
    fun savePlayerInteractedWithEvent() {
        val videoId = aValidId()
        eventService.savePlayerInteractedWithEvent(
            videoId = VideoId(videoId),
            currentTime = 34,
            subtype = "captions-on",
            payload = mapOf<String, Any>(
                Pair("kind", "caption-kind"),
                Pair("language", "caption-language"),
                Pair("id", "caption-id"),
                Pair("label", "caption-label")
            ),
            user = UserFactory.sample(id = "user@example.com")
        )

        val event = fakeEventBus.getEventOfType(VideoPlayerInteractedWith::class.java)

        assertThat(event.userId).isEqualTo("user@example.com")
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
        eventService.publishVideoInteractedWithEvent(
            videoId = VideoId(videoId),
            subtype = "share-to-google-classroom",
            user = UserFactory.sample(id = "user@example.com")
        )

        val event = fakeEventBus.getEventOfType(VideoInteractedWith::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.subtype).isEqualTo("share-to-google-classroom")
        assertThat(event.userId).isEqualTo("user@example.com")
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

    @Test
    fun `events use overriding ID when present`() {
        val videoId = aValidId()
        val overridingId = "overriding-id"
        eventService.publishVideoInteractedWithEvent(
            videoId = VideoId(videoId),
            subtype = "share-to-google-classroom",
            user = UserFactory.sample(id = "user@example.com", eventIdSupplier = { overridingId })
        )

        val event = fakeEventBus.getEventOfType(VideoInteractedWith::class.java)

        assertThat(event.userId).isEqualTo(overridingId)
    }
}
