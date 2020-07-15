package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoVideoRepositoryStreamingIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    @Test
    fun `stream all`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId()
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId()
            )
        )

        var videos: List<Video> = emptyList()

        mongoVideoRepository.streamAll { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `stream all by legacy type`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                types = listOf(ContentType.STOCK)
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                types = listOf(ContentType.INSTRUCTIONAL_CLIPS)
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                types = listOf(ContentType.NEWS)
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.HasContentType(ContentType.INSTRUCTIONAL_CLIPS)) {
            videos = it.toList()
        }

        assertThat(videos).hasSize(1)
    }

    @Test
    fun `stream all by content partner name`() {
        mongoVideoRepository.create(TestFactories.createVideo(channelName = "TED"))
        mongoVideoRepository.create(TestFactories.createVideo(channelName = "Bob"))
        mongoVideoRepository.create(TestFactories.createVideo(channelName = "TED"))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.ChannelNameIs("TED")) { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `stream all by content partner id`() {
        val contentPartnerIdToFind = ObjectId().toHexString()
        mongoVideoRepository.create(
            TestFactories.createVideo(
                channelId = ChannelId(
                    value = contentPartnerIdToFind
                )
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                channelId = ChannelId(
                    value = contentPartnerIdToFind
                )
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                channelId = ChannelId(
                    value = ObjectId().toHexString()
                )
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(
            VideoFilter.ChannelIdIs(
                ChannelId(
                    value = contentPartnerIdToFind
                )
            )
        ) {
            videos = it.toList()
        }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `stream all by video ids`() {
        val video1 = TestFactories.createVideo()
        val video2 = TestFactories.createVideo()
        mongoVideoRepository.create(TestFactories.createVideo())
        mongoVideoRepository.create(video1)
        mongoVideoRepository.create(video2)
        mongoVideoRepository.create(TestFactories.createVideo())

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.HasVideoId(video1.videoId, video2.videoId)) { videos = it.toList() }

        assertThat(videos).hasSize(2)
        assertThat(videos.first().videoId).isEqualTo(video1.videoId)
        assertThat(videos.last().videoId).isEqualTo(video2.videoId)
    }

    @Test
    fun `stream all by youtube`() {
        mongoVideoRepository.create(TestFactories.createVideo(playback = TestFactories.createYoutubePlayback()))
        mongoVideoRepository.create(TestFactories.createVideo(playback = TestFactories.createKalturaPlayback()))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsYoutube) { videos = it.toList() }

        assertThat(videos).hasSize(1)
        assertThat(videos.first().playback.id.type).isEqualTo(PlaybackProviderType.YOUTUBE)
    }

    @Test
    fun `stream all by kaltura`() {
        mongoVideoRepository.create(TestFactories.createVideo(playback = TestFactories.createYoutubePlayback()))
        mongoVideoRepository.create(TestFactories.createVideo(playback = TestFactories.createKalturaPlayback()))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsKaltura) { videos = it.toList() }

        assertThat(videos).hasSize(1)
        assertThat(videos.first().playback.id.type).isEqualTo(PlaybackProviderType.KALTURA)
    }

    @Test
    fun `stream all by subject id`() {
        val mathsSubject = TestFactories.createSubject(name = "Maths")
        val englishSubject = TestFactories.createSubject(name = "English")

        val video1 =
            mongoVideoRepository.create(TestFactories.createVideo(subjects = setOf(mathsSubject, englishSubject)))
        val video2 = mongoVideoRepository.create(TestFactories.createVideo(subjects = setOf(mathsSubject)))
        mongoVideoRepository.create(TestFactories.createVideo(subjects = setOf()))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.HasSubjectId(mathsSubject.id)) {
            videos = it.toList()
        }

        assertThat(videos).containsExactlyInAnyOrder(video1, video2)
    }

    @Test
    fun `stream all by deactivated`() {
        val video1 = mongoVideoRepository.create(TestFactories.createVideo(deactivated = true))
        val video2 = mongoVideoRepository.create(TestFactories.createVideo(deactivated = true))
        mongoVideoRepository.create(TestFactories.createVideo(deactivated = false))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsDeactivated) {
            videos = it.toList()
        }

        assertThat(videos).containsExactlyInAnyOrder(video1, video2)
    }

    @Test
    fun `update all videos filtered by given subject`() {
        val mathsSubject = TestFactories.createSubject(name = "Maths")
        val englishSubject = TestFactories.createSubject(name = "English")

        val video1 =
            mongoVideoRepository.create(TestFactories.createVideo(subjects = setOf(mathsSubject, englishSubject)))
        val video2 = mongoVideoRepository.create(TestFactories.createVideo(subjects = setOf()))

        mongoVideoRepository.streamUpdate(VideoFilter.HasSubjectId(mathsSubject.id)) { videos: List<Video> ->
            videos.map { video ->
                VideoUpdateCommand.ReplaceSubjects(video.videoId, listOf(Subject(mathsSubject.id, "MathsIsForLosers")))
            }
        }

        assertThat(mongoVideoRepository.find(videoId = video1.videoId)!!.subjects.items.map { it.name })
            .containsExactlyInAnyOrder("MathsIsForLosers")

        assertThat(mongoVideoRepository.find(videoId = video2.videoId)!!.subjects.items).isEmpty()
    }

    @Test
    fun `stream update returns updated videos in sequence`() {
        mongoVideoRepository.create(TestFactories.createVideo(title = "Old Title"))

        val updatedVideos: Sequence<Video> = mongoVideoRepository.streamUpdate(VideoFilter.IsKaltura) { videos: List<Video> ->
            videos.map { video ->
                VideoUpdateCommand.ReplaceTitle(video.videoId, "New Title")
            }
        }

        assertThat(updatedVideos.toList().first().title).isEqualTo("New Title")
    }
}
