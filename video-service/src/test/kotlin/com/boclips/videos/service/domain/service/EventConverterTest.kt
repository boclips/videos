package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.Subject
import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class EventConverterTest {
    @Test
    fun `creates a video event object`() {
        val id = TestFactories.aValidId()
        val video = TestFactories.createVideo(
            videoId = id,
            title = "the title",
            contentPartnerName = "the content partner",
            playback = TestFactories.createKalturaPlayback(duration = Duration.ofMinutes(2)),
            subjects = setOf(TestFactories.createSubject(name = "physics")),
            ageRange = AgeRange.bounded(5, 10)
        )

        val videoEvent = EventConverter().toVideoPayload(video)

        assertThat(videoEvent.id.value).isEqualTo(id)
        assertThat(videoEvent.title).isEqualTo("the title")
        assertThat(videoEvent.contentPartner.name).isEqualTo("the content partner")
        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.KALTURA)
        assertThat(videoEvent.subjects).hasSize(1)
        assertThat(videoEvent.subjects.first().name).isEqualTo("physics")
        assertThat(videoEvent.ageRange.min).isEqualTo(5)
        assertThat(videoEvent.ageRange.max).isEqualTo(10)
        assertThat(videoEvent.durationSeconds).isEqualTo(120)
    }

    @Test
    fun `sets correct playback provider type when YouTube`() {
        val video = TestFactories.createVideo(
                playback = TestFactories.createYoutubePlayback()
        )

        val videoEvent = EventConverter().toVideoPayload(video)

        assertThat(videoEvent.playbackProviderType).isEqualTo(PlaybackProviderType.YOUTUBE)
    }

    @Test
    fun `creates a collection event object`() {
        val id = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()
        val publicCollection = TestFactories.createCollection(
            id = CollectionId(id),
            title = "collection title",
            isPublic = true,
            videos = listOf(VideoId(videoId)),
            subjects = setOf(TestFactories.createSubject(id = "subject-id", name = "subject name")),
            owner = "user-id",
            ageRangeMin = 0,
            ageRangeMax = 23,
            bookmarks = setOf(UserId("bookmarked-user-id"))

        )
        val privateCollection = TestFactories.createCollection(isPublic = false)

        val publicCollectionEvent = EventConverter().toCollectionPayload(publicCollection)
        val privateCollectionEvent = EventConverter().toCollectionPayload(privateCollection)

        assertThat(publicCollectionEvent.id.value).isEqualTo(id)
        assertThat(publicCollectionEvent.title).isEqualTo("collection title")
        assertThat(publicCollectionEvent.description).isEqualTo("collection description")
        assertThat(publicCollectionEvent.visible).isTrue()
        assertThat(privateCollectionEvent.visible).isFalse()
        assertThat(publicCollectionEvent.videosIds).containsExactly(com.boclips.eventbus.domain.video.VideoId(videoId))
        assertThat(publicCollectionEvent.subjects).containsExactly(Subject(SubjectId("subject-id"), "subject name"))
        assertThat(publicCollectionEvent.ownerId.value).isEqualTo("user-id")
        assertThat(publicCollectionEvent.ageRange).isEqualTo(com.boclips.eventbus.domain.AgeRange(0, 23))
        assertThat(publicCollectionEvent.bookmarks).containsExactly(com.boclips.eventbus.domain.user.UserId("bookmarked-user-id"))
    }
}
