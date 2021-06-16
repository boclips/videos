package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createKalturaPlayback
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.VideoFactory.createVideoAsset
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.Date

class MongoVideoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    val maths = TestFactories.createSubject(name = "Maths")
    val biology = TestFactories.createSubject(name = "Biology")

    @Test
    fun `create a video`() {
        val video = createVideo(playback = createKalturaPlayback(assets = setOf(createVideoAsset())))
        val createdVideo = mongoVideoRepository.create(video)

        assertThat(createdVideo).isEqualToIgnoringGivenFields(video, "playback")
        assertThat(createdVideo.playback).isInstanceOf(StreamPlayback::class.java)
        assertThat((createdVideo.playback as StreamPlayback).assets).isNotEmpty
    }

    @Test
    fun `find a video`() {
        val originalVideo = mongoVideoRepository.create(
            createVideo(
                playback = createKalturaPlayback(
                    assets = setOf(
                        createVideoAsset()
                    )
                )
            )
        )
        val retrievedVideo = mongoVideoRepository.find(originalVideo.videoId)!!

        assertThat(retrievedVideo).isEqualTo(originalVideo)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(VideoId(TestFactories.aValidId()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = mongoVideoRepository.create(createVideo()).videoId
        val id2 = mongoVideoRepository.create(createVideo()).videoId
        val id3 = mongoVideoRepository.create(createVideo()).videoId

        val videos = mongoVideoRepository.findAll(listOf(id2, id1, id3))

        assertThat(videos.map { it.videoId }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `lookup videos only return videos once`() {
        val id1 = mongoVideoRepository.create(createVideo()).videoId

        val videos = mongoVideoRepository.findAll(listOf(id1, id1, id1))

        assertThat(videos.map { it.videoId }).containsExactly(id1)
    }

    @Test
    fun `delete a video`() {
        val id = TestFactories.aValidId()
        val originalAsset = createVideo(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(VideoId(id))

        assertThat(mongoVideoRepository.find(VideoId(id))).isNull()
    }

    @Test
    fun `find by content partner name and content partner video id`() {
        val video = createVideo(
            videoId = TestFactories.aValidId(),
            channelName = "TED Talks",
            channelVideoId = "ted-id-1"
        )

        mongoVideoRepository.create(video)

        assertThat(mongoVideoRepository.existsVideoFromChannelName("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromChannelName("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromChannelName("TED Talks abc", "ted-id-1")).isFalse()
    }

    @Test
    fun `find by content partner id and content partner video id`() {
        val channelId =
            ChannelId(value = "5d319070871956b43f45eb82")

        val video = createVideo(
            videoId = TestFactories.aValidId(),
            channelVideoId = "ted-id-1",
            channelId = channelId
        )

        mongoVideoRepository.create(video)

        assertThat(mongoVideoRepository.existsVideoFromChannelId(channelId, "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromChannelId(channelId, "ted-id-2")).isFalse()

        assertThat(
            mongoVideoRepository.existsVideoFromChannelId(
                ChannelId(value = ObjectId().toHexString()),
                "ted-id-1"
            )
        ).isFalse()
        assertThat(
            mongoVideoRepository.existsVideoFromChannelId(
                ChannelId("invalid-hex-string"),
                "ted-id-1"
            )
        ).isFalse()
    }

    @Test
    fun `finds videos by content partner`() {
        val video1 =
            mongoVideoRepository.create(createVideo(title = "Video 1", channelName = "TED"))
        val video2 =
            mongoVideoRepository.create(createVideo(title = "Video 2", channelName = "TED"))
        val video3 =
            mongoVideoRepository.create(createVideo(title = "Video 3", channelName = "Reuters"))

        val videos = mongoVideoRepository.findByChannelName(channelName = "TED")

        assertThat(videos).contains(video1)
        assertThat(videos).contains(video2)
        assertThat(videos).doesNotContain(video3)
    }

    @Test
    fun `find videos by content partner youtube id`() {
        val channelId = ObjectId().toHexString()
        val video1 =
            mongoVideoRepository.create(
                createVideo(
                    title = "Video 1",
                    channelId = ChannelId(
                        value = channelId
                    )
                )
            )

        val video2 =
            mongoVideoRepository.create(
                createVideo(
                    title = "Video 2",
                    channelId = ChannelId(
                        value = channelId
                    )
                )
            )

        mongoVideoRepository.create(
            createVideo(
                title = "Video 3",
                channelId = ChannelId(
                    value = ObjectId().toHexString()
                )
            )
        )

        val videos =
            mongoVideoRepository.findByChannelId(
                channelId = ChannelId(
                    value = channelId
                )
            )

        assertThat(videos).containsExactly(video1, video2)
    }

    @Test
    fun `find videos by content partner id`() {
        val id = ObjectId.get().toHexString()
        val video1 =
            mongoVideoRepository.create(
                createVideo(
                    title = "Video 1",
                    channelId = ChannelId(
                        id
                    )
                )
            )
        val video2 =
            mongoVideoRepository.create(
                createVideo(
                    title = "Video 2",
                    channelId = ChannelId(
                        id
                    )
                )
            )

        val id2 = ObjectId.get().toHexString()
        mongoVideoRepository.create(
            createVideo(
                title = "Video 3",
                channelId = ChannelId(
                    id2
                )
            )
        )

        val videos = mongoVideoRepository.findByChannelId(
            channelId = ChannelId(
                id
            )
        )
        assertThat(videos).containsExactly(video1, video2)
    }

    @Test
    fun `check if video exists by title and content partner name`() {
        val video = mongoVideoRepository.create(
            createVideo(
                title = "Video 1",
                channelName = "TestCP"
            )
        )

        val matchedVideo = mongoVideoRepository.findVideoByTitleFromChannelName(
            channelName = "TestCP",
            videoTitle = "Video 1"
        )
        assertThat(matchedVideo).isEqualTo(video)
    }

    @Nested
    @DisplayName(value = "Migration Tests")
    inner class MigrationTests {
        @Test
        fun `can retrieve legacy video documents but they are not playable`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoVideoRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "Mah Video")
                        .append("description", "Ain't no video like this one")
                        .append(
                            "source",
                            Document()
                                .append(
                                    "channel",
                                    Document()
                                        .append("name", "cp-name")
                                        .append("_id", ObjectId())
                                )
                                .append("videoReference", "ref")
                        )
                        .append("playback", Document().append("id", "some-id").append("type", "KALTURA"))
                        .append("contentType", VideoType.NEWS.name)
                        .append("keywords", emptyList<String>())
                        .append("subjects", emptyList<String>())
                        .append("releaseDate", Date())
                        .append("durationSeconds", 10)
                        .append("legalRestrictions", "Some restrictions")
                        .append("language", "en")
                        .append("searchable", true)
                )

            val video = mongoVideoRepository.find(VideoId(value = "5c55697860fef77aa4af323a"))

            assertThat(video).isNotNull
            assertThat(video!!.isPlayable()).isFalse()
        }

        @Test
        fun `can parse valid kaltura video and video is playable`() {
            mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(MongoVideoRepository.collectionName)
                .insertOne(
                    Document()
                        .append("_id", ObjectId("5c55697860fef77aa4af323a"))
                        .append("title", "Mah Video")
                        .append("description", "Ain't no video like this one")
                        .append(
                            "source",
                            Document()
                                .append(
                                    "channel",
                                    Document()
                                        .append("name", "cp-name")
                                        .append("_id", ObjectId())
                                )
                                .append("videoReference", "ref")
                        )
                        .append(
                            "playback",
                            Document()
                                .append("id", "some-id")
                                .append("type", "KALTURA")
                                .append("thumbnailUrl", listOf("thumbnail"))
                                .append("downloadUrl", "x")
                                .append("dashStreamUrl", "x")
                                .append("hlsStreamUrl", "x")
                                .append("progressiveStreamUrl", "x")
                                .append("duration", 10)
                                .append("entryId", "some-entry-id")
                        )
                        .append("contentType", VideoType.NEWS.name)
                        .append("keywords", emptyList<String>())
                        .append("subjects", emptyList<String>())
                        .append("releaseDate", Date())
                        .append("durationSeconds", 10)
                        .append("legalRestrictions", "Some restrictions")
                        .append("language", "en")
                        .append("searchable", true)
                        .append("distributionMethods", emptySet<DistributionMethodDocument>())
                )

            val video = mongoVideoRepository.find(VideoId(value = "5c55697860fef77aa4af323a"))!!

            assertThat(video.isPlayable()).isTrue()
            assertThat(video.title).isEqualTo("Mah Video")
            assertThat(video.description).isEqualTo("Ain't no video like this one")
            assertThat(video.playback.id.value).isEqualTo("some-entry-id")
            assertThat(video.playback.id.type.name).isEqualTo("KALTURA")
        }
    }
}
