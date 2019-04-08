package com.boclips.videos.service.infrastructure.analytics

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.Date

class MongoAnalyticsEventServiceIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var mongoEventService: MongoAnalyticsEventService

    @BeforeEach
    fun setUp() {
        mongoEventService = MongoAnalyticsEventService(mongoClient)
        setSecurityContext("user@example.com")
    }

    @Test
    fun `saving search events`() {
        mongoEventService.saveSearchEvent(
            query = "the query",
            pageIndex = 1,
            pageSize = 10,
            totalResults = 789
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("SEARCH")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["query"]).isEqualTo("the query")
        assertThat(event["pageIndex"]).isEqualTo(1)
        assertThat(event["pageSize"]).isEqualTo(10)
        assertThat(event["totalResults"]).isEqualTo(789L)
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
    }

    @Test
    fun `internal users' events are flagged`() {
        setSecurityContext("user@boclips.com")
        mongoEventService.saveSearchEvent(query = "", pageIndex = 0, pageSize = 0, totalResults = 1)

        val event = getEvent()

        assertThat(event["userIsBoclips"]).isEqualTo(true)
    }

    @Test
    fun `url is null when Referer header not present`() {
        mongoEventService.saveSearchEvent(query = "", pageIndex = 0, pageSize = 0, totalResults = 1)

        val event = getEvent()

        assertThat(event["url"]).isNull()
    }

    @Test
    fun `url is set when Referer header is present`() {
        val request = MockHttpServletRequest()
        request.addHeader("Referer", "https://boclips.com/somepage")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        mongoEventService.saveSearchEvent(query = "", pageIndex = 0, pageSize = 0, totalResults = 1)

        val event = getEvent()

        assertThat(event["url"]).isEqualTo("https://boclips.com/somepage")
    }

    @Test
    fun `saving playback events`() {
        val videoId = TestFactories.aValidId()
        mongoEventService.savePlaybackEvent(
            videoId = AssetId(videoId),
            videoIndex = 6,
            playerId = "player id",
            segmentStartSeconds = 10,
            segmentEndSeconds = 20,
            videoDurationSeconds = 40
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("PLAYBACK")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["assetId"]).isEqualTo(videoId)
        assertThat(event["playerId"]).isEqualTo("player id")
        assertThat(event["segmentStartSeconds"]).isEqualTo(10L)
        assertThat(event["segmentEndSeconds"]).isEqualTo(20L)
        assertThat(event["videoDurationSeconds"]).isEqualTo(40L)
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
    }

    @Test
    fun `saving add to collection events`() {
        val videoId = TestFactories.aValidId()
        mongoEventService.saveUpdateCollectionEvent(
            CollectionId("collection id"),
            listOf(CollectionUpdateCommand.AddVideoToCollectionCommand(AssetId(videoId)))
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("ADD_TO_COLLECTION")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["assetId"]).isEqualTo(videoId)
        assertThat(event["collectionId"]).isEqualTo("collection id")
    }

    @Test
    fun `saving remove from collection events`() {
        val videoId = TestFactories.aValidId()
        mongoEventService.saveUpdateCollectionEvent(
            CollectionId("collection id"),
            listOf(CollectionUpdateCommand.RemoveVideoFromCollectionCommand(AssetId(videoId)))
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("REMOVE_FROM_COLLECTION")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["assetId"]).isEqualTo(videoId)
        assertThat(event["collectionId"]).isEqualTo("collection id")
    }

    @Test
    fun `saving rename collection event`() {
        val collectionId = TestFactories.aValidId()
        mongoEventService.saveUpdateCollectionEvent(
            CollectionId(collectionId),
            listOf(CollectionUpdateCommand.RenameCollectionCommand(title = "a new title"))
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("RENAME_COLLECTION")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["collectionId"]).isEqualTo(collectionId)
        assertThat(event["title"]).isEqualTo("a new title")
    }

    @Test
    fun `saving bookmark collection event`() {
        val collectionId = TestFactories.aValidId()
        mongoEventService.saveBookmarkCollectionEvent(
            CollectionId(collectionId)
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("BOOKMARK")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["collectionId"]).isEqualTo(collectionId)
    }

    @Test
    fun `saving Unbookmark collection event`() {
        val collectionId = TestFactories.aValidId()
        mongoEventService.saveUnbookmarkCollectionEvent(
            CollectionId(collectionId)
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("UNBOOKMARK")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["collectionId"]).isEqualTo(collectionId)
    }

    @Test
    fun `saving change visibility event`() {
        val collectionId = TestFactories.aValidId()
        mongoEventService.saveUpdateCollectionEvent(
            CollectionId(collectionId),
            listOf(CollectionUpdateCommand.ChangeVisibilityCommand(isPublic = false))
        )

        val event = getEvent()

        assertThat(event["type"]).isEqualTo("CHANGE_VISIBILITY")
        assertThat(event["timestamp"] as Date).isAfter("2019-02-10")
        assertThat(event["userId"]).isEqualTo("user@example.com")
        assertThat(event["userIsBoclips"]).isEqualTo(false)
        assertThat(event["collectionId"]).isEqualTo(collectionId)
        assertThat(event["isPublic"]).isEqualTo(false)
    }

    private fun getEvent(): Document {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection(MongoAnalyticsEventService.collectionName).find()
            .single()
    }
}
