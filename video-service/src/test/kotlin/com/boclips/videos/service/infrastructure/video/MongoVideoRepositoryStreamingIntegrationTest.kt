package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
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
    fun `stream all streamable videos`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                distributionMethods = setOf(DistributionMethod.DOWNLOAD)
            )
        )

        val streamableVideos = listOf(
            createVideoWithLegacyDistributionMethods(),
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    videoId = TestFactories.aValidId(),
                    distributionMethods = setOf(DistributionMethod.STREAM)
                )
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsStreamable) { videos = it.toList() }

        assertThat(videos).isEqualTo(streamableVideos)
    }

    @Test
    fun `stream all downloadable videos`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                distributionMethods = setOf(DistributionMethod.STREAM)
            )
        )

        val downloadableVideos = listOf(
            createVideoWithLegacyDistributionMethods(),
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    videoId = TestFactories.aValidId(),
                    distributionMethods = setOf(DistributionMethod.DOWNLOAD)
                )
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.IsDownloadable) { videos = it.toList() }

        assertThat(videos).isEqualTo(downloadableVideos)
    }

    @Test
    fun `stream all by legacy type`() {
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                type = LegacyVideoType.STOCK
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                type = LegacyVideoType.INSTRUCTIONAL_CLIPS
            )
        )
        mongoVideoRepository.create(
            TestFactories.createVideo(
                videoId = TestFactories.aValidId(),
                type = LegacyVideoType.NEWS
            )
        )

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.LegacyTypeIs(LegacyVideoType.INSTRUCTIONAL_CLIPS)) {
            videos = it.toList()
        }

        assertThat(videos).hasSize(1)
    }

    @Test
    fun `stream all by content partner name`() {
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerName = "TED"))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerName = "Bob"))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerName = "TED"))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.ContentPartnerNameIs("TED")) { videos = it.toList() }

        assertThat(videos).hasSize(2)
    }

    @Test
    fun `stream all by content partner id`() {
        val contentPartnerIdToFind = ObjectId().toHexString()
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = ContentPartnerId(value = contentPartnerIdToFind)))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = ContentPartnerId(value = contentPartnerIdToFind)))
        mongoVideoRepository.create(TestFactories.createVideo(contentPartnerId = ContentPartnerId(value = ObjectId().toHexString())))

        var videos: List<Video> = emptyList()
        mongoVideoRepository.streamAll(VideoFilter.ContentPartnerIdIs(ContentPartnerId(value = contentPartnerIdToFind))) {
            videos = it.toList()
        }

        assertThat(videos).hasSize(2)
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
        val englishSubject = TestFactories.createSubject(name = "Englihs")

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

    private fun createVideoWithLegacyDistributionMethods(): Video {
        val legacyVideo = mongoVideoRepository.create(TestFactories.createVideo())

        mongoClient
            .getDatabase(DATABASE_NAME)
            .getCollection(MongoVideoRepository.collectionName)
            .updateOne(
                VideoDocument::id eq ObjectId(legacyVideo.videoId.value),
                org.litote.kmongo.set(VideoDocument::distributionMethods, null)
            )

        return mongoVideoRepository.find(legacyVideo.videoId)!!
    }
}
