package com.boclips.videos.service.client.internal.resources

import com.boclips.videos.service.client.VideoId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.Month

class CollectionResourceTest {
    @Test
    fun `converts resource to collection`() {
        val collectionUri = "https://video-service.com/v1/collections/cof1fadef1fadef1fadef1fa"
        val videoId = "def1fadef1fadef1fadef1fa"

        val releaseDate = LocalDate.of(2000, Month.JANUARY, 5)
        val resource = CollectionResource().apply {
            _links = CollectionLinks(Link(collectionUri))
            title = "the title"
            description = "the desc"
            isPublic = true
            subjects = setOf(SubjectResource().apply {
                this.id = "maths-123"
                this.name = "Maths"
            })
            videos = listOf(VideoResource().apply {
                _links =
                    VideoLinks().apply { self = Link().apply { href = "https://video-service.com/v1/videos/$videoId" } }
                title = "The best video"
                description = "Seriously, the best thing you're gonna see"
                releasedOn = releaseDate
                playback = PlaybackResource.builder()
                    .id("test-playback-id")
                    .duration(Duration.ofMinutes(7))
                    .thumbnailUrl("https://thumbz.org/123/456")
                    ._links(
                        PlaybackLinksResource.builder().createPlaybackEvent(Link("/events/playback")).build()
                    )
                    .build()
                contentPartnerVideoId = "content-partner-video-id"
                contentPartnerId = "Best partner id 1"
                createdBy = "Best partner"
            })
        }

        val collection = resource.toCollection()
        assertThat(collection.collectionId.uri.toString()).isEqualTo(collectionUri)
        assertThat(collection.title).isEqualTo("the title")
        assertThat(collection.description).isEqualTo("the desc")
        assertThat(collection.subjects.map { it.name }).containsExactly("Maths")
        assertThat(collection.videos).isNotEmpty
        assertThat(collection.isPublic).isTrue()

        val video = collection.videos.component1()
        assertThat(video.videoId).isEqualTo(VideoId(URI("https://video-service.com/v1/videos/def1fadef1fadef1fadef1fa")))
        assertThat(video.title).isEqualTo("The best video")
        assertThat(video.description).isEqualTo("Seriously, the best thing you're gonna see")
        assertThat(video.releasedOn).isEqualTo(releaseDate)
        assertThat(video.playback.playbackId).isEqualTo("test-playback-id")
        assertThat(video.playback.duration).isEqualTo(Duration.ofMinutes(7))
        assertThat(video.playback.thumbnailUrl).isEqualTo("https://thumbz.org/123/456")
        assertThat(video.playback.links.createPlaybackEvent.href).endsWith("/events/playback")
        assertThat(video.contentPartnerVideoId).isEqualTo("content-partner-video-id")
        assertThat(video.createdBy).isEqualTo("Best partner")
        assertThat(video.contentPartnerId).isEqualTo("Best partner id 1")
    }

    @Test
    fun `maps playback to null if it's not returned on the resource`() {
        val resource = CollectionResource().apply {
            _links = CollectionLinks(Link("https://video-service.com/v1/collections/cof1fadef1fadef1fadef1fa"))
            title = "the title"
            subjects = setOf(SubjectResource().apply { this.id = "maths" })
            videos = listOf(VideoResource().apply {
                _links = VideoLinks().apply {
                    self = Link().apply { href = "https://video-service.com/v1/videos/def1fadef1fadef1fadef1fa" }
                }
            })
        }

        assertThat(resource.toCollection().videos.component1().playback).isNull()
    }
}
