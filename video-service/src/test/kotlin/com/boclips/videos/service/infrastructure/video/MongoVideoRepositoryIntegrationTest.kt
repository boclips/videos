package com.boclips.videos.service.infrastructure.video

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Date
import java.util.Locale

class MongoVideoRepositoryIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    val maths = TestFactories.createSubject(name = "Maths")
    val biology = TestFactories.createSubject(name = "Biology")

    @Test
    fun `create a video`() {
        val video = TestFactories.createVideo()
        val createdAsset = mongoVideoRepository.create(video)

        assertThat(createdAsset).isEqualToIgnoringGivenFields(video, "playback")
    }

    @Test
    fun `find a video`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideo())
        val retrievedAsset = mongoVideoRepository.find(originalAsset.videoId)!!

        assertThat(retrievedAsset).isEqualTo(originalAsset)
    }

    @Test
    fun `find video when does not exist`() {
        assertThat(mongoVideoRepository.find(VideoId(TestFactories.aValidId()))).isNull()
    }

    @Test
    fun `lookup videos by ids maintains video order`() {
        val id1 = mongoVideoRepository.create(TestFactories.createVideo()).videoId
        val id2 = mongoVideoRepository.create(TestFactories.createVideo()).videoId
        val id3 = mongoVideoRepository.create(TestFactories.createVideo()).videoId

        val videos = mongoVideoRepository.findAll(listOf(id2, id1, id3))

        assertThat(videos.map { it.videoId }).containsExactly(id2, id1, id3)
    }

    @Test
    fun `lookup videos only return videos once`() {
        val id1 = mongoVideoRepository.create(TestFactories.createVideo()).videoId

        val videos = mongoVideoRepository.findAll(listOf(id1, id1, id1))

        assertThat(videos.map { it.videoId }).containsExactly(id1)
    }

    @Test
    fun `delete a video`() {
        val id = TestFactories.aValidId()
        val originalAsset = TestFactories.createVideo(videoId = id)

        mongoVideoRepository.create(originalAsset)
        mongoVideoRepository.delete(VideoId(id))

        assertThat(mongoVideoRepository.find(VideoId(id))).isNull()
    }

    @Test
    fun `update playback`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                playback = TestFactories.createKalturaPlayback(entryId = "original-entry")
            )
        )

        mongoVideoRepository.bulkUpdate(
            listOf(
                VideoUpdateCommand.ReplacePlayback(
                    originalAsset.videoId,
                    TestFactories.createKalturaPlayback(
                        duration = Duration.ZERO,
                        entryId = "new-entry",
                        downloadUrl = "download-url-updated",
                        hlsStreamUrl = "stream-url-updated",
                        dashStreamUrl = "dash-url-updated"
                    )
                )
            )
        )

        val updatedAsset = mongoVideoRepository.find(originalAsset.videoId)

        assertThat(updatedAsset!!.playback).isNotNull
        assertThat(updatedAsset.playback.id.value).isEqualTo("new-entry")
        assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ZERO)
        assertThat((updatedAsset.playback as StreamPlayback).downloadUrl).isEqualTo("download-url-updated")
        assertThat((updatedAsset.playback as StreamPlayback).appleHlsStreamUrl).isEqualTo("stream-url-updated")
        assertThat((updatedAsset.playback as StreamPlayback).mpegDashStreamUrl).isEqualTo("dash-url-updated")
    }

    @Test
    fun `update title`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideo(title = "old title"))

        val updatedAsset =
            mongoVideoRepository.update(VideoUpdateCommand.ReplaceTitle(originalAsset.videoId, "new title"))

        assertThat(updatedAsset.title).isEqualTo("new title")
    }

    @Test
    fun `update description`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideo(description = "old description"))

        val updatedAsset =
            mongoVideoRepository.update(VideoUpdateCommand.ReplaceDescription(originalAsset.videoId, "new description"))

        assertThat(updatedAsset.description).isEqualTo("new description")
    }

    @Test
    fun `update subjects`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title",
                subjects = setOf(maths)
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceSubjects(
                originalAsset.videoId,
                listOf(biology)
            )
        )

        assertThat(updatedAsset).isEqualToIgnoringGivenFields(originalAsset, "subjects")
        assertThat(updatedAsset.subjects).containsOnly(biology)
    }

    @Test
    fun `update age range`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title"
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceAgeRange(
                originalAsset.videoId,
                AgeRange.bounded(3, 5)
            )
        )

        assertThat(updatedAsset.ageRange).isEqualTo(AgeRange.bounded(3, 5))
    }

    @Test
    fun `update user rating`() {
        val originalAsset = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title"
            )
        )

        mongoVideoRepository.update(
            VideoUpdateCommand.AddRating(
                originalAsset.videoId,
                UserRating(3, UserId("a user"))
            )
        )
        mongoVideoRepository.update(
            VideoUpdateCommand.AddRating(
                originalAsset.videoId,
                UserRating(3, UserId("another user"))
            )
        )
        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.AddRating(
                originalAsset.videoId,
                UserRating(5, UserId("another user"))
            )
        )

        assertThat(updatedAsset.ratings).containsExactlyInAnyOrder(
            UserRating(3, UserId("a user")),
            UserRating(5, UserId("another user"))
        )
    }

    @Test
    fun `update video tag`() {
        val originalAsset = mongoVideoRepository.create(TestFactories.createVideo())
        val tag = TestFactories.createUserTag(label = "Alex", userId = "user-1")
        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceTag(
                originalAsset.videoId, tag

            )
        )

        assertThat(updatedAsset.tag!!.tag.label).isEqualTo("Alex")
    }

    @Test
    fun `update throws when video not found`() {
        assertThrows<VideoNotFoundException> {
            mongoVideoRepository.update(
                VideoUpdateCommand.ReplaceDuration(
                    VideoId(value = TestFactories.aValidId()),
                    duration = Duration.ZERO
                )
            )
        }
    }

    @Test
    fun `bulk update applies multiple independent updates at once`() {
        val originalVideo1 = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title 1",
                playback = TestFactories.createKalturaPlayback(
                    duration = Duration.ofMinutes(1)
                ),
                subjects = setOf(maths)
            )
        )

        val originalVideo2 = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "original title 2",
                playback = TestFactories.createKalturaPlayback(
                    duration = Duration.ofMinutes(99)
                ),
                subjects = setOf(maths)
            )
        )

        val updates = listOf(
            VideoUpdateCommand.ReplaceSubjects(
                videoId = originalVideo1.videoId,
                subjects = emptyList()
            ),
            VideoUpdateCommand.ReplaceDuration(
                videoId = originalVideo1.videoId,
                duration = Duration.ofMinutes(10)
            ),
            VideoUpdateCommand.ReplaceSubjects(
                videoId = originalVideo2.videoId,
                subjects = listOf(biology)
            ),
            VideoUpdateCommand.ReplaceDuration(
                videoId = originalVideo2.videoId,
                duration = Duration.ofMinutes(11)
            )
        )

        val updatedVideos = mongoVideoRepository.bulkUpdate(updates)

        val updatedVideo1 = updatedVideos.find { it.videoId == originalVideo1.videoId }!!
        val updatedVideo2 = updatedVideos.find { it.videoId == originalVideo2.videoId }!!

        assertThat(updatedVideo1).isEqualToIgnoringGivenFields(originalVideo1, "subjects", "duration", "playback")
        assertThat(updatedVideo1.playback.duration).isEqualTo(Duration.ofMinutes(10))
        assertThat(updatedVideo1.subjects).isEmpty()

        assertThat(updatedVideo2).isEqualToIgnoringGivenFields(originalVideo2, "subjects", "duration", "playback")
        assertThat(updatedVideo2.playback.duration).isEqualTo(Duration.ofMinutes(11))
        assertThat(updatedVideo2.subjects).isEqualTo(setOf(biology))
    }

    @Test
    fun `find by content partner name and content partner video id`() {
        val video = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            contentPartnerName = "TED Talks",
            contentPartnerVideoId = "ted-id-1"
        )

        mongoVideoRepository.create(video)

        assertThat(mongoVideoRepository.existsVideoFromContentPartnerName("TED Talks", "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartnerName("TED Talks", "ted-id-2")).isFalse()
        assertThat(mongoVideoRepository.existsVideoFromContentPartnerName("TED Talks abc", "ted-id-1")).isFalse()
    }

    @Test
    fun `find by content partner id and content partner video id`() {
        val contentPartnerId =
            ContentPartnerId(value = "5d319070871956b43f45eb82")

        val video = TestFactories.createVideo(
            videoId = TestFactories.aValidId(),
            contentPartnerVideoId = "ted-id-1",
            contentPartnerId = contentPartnerId
        )

        mongoVideoRepository.create(video)

        assertThat(mongoVideoRepository.existsVideoFromContentPartnerId(contentPartnerId, "ted-id-1")).isTrue()
        assertThat(mongoVideoRepository.existsVideoFromContentPartnerId(contentPartnerId, "ted-id-2")).isFalse()

        assertThat(
            mongoVideoRepository.existsVideoFromContentPartnerId(
                ContentPartnerId(value = ObjectId().toHexString()),
                "ted-id-1"
            )
        ).isFalse()
        assertThat(
            mongoVideoRepository.existsVideoFromContentPartnerId(
                ContentPartnerId("invalid-hex-string"),
                "ted-id-1"
            )
        ).isFalse()
    }

    @Test
    fun `can update hidden from search for delivery methods`() {
        val video =
            mongoVideoRepository.create(TestFactories.createVideo(distributionMethods = emptySet()))
        mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceDistributionMethods(
                video.videoId,
                setOf(DistributionMethod.DOWNLOAD, DistributionMethod.STREAM)
            )
        )
        assertThat(mongoVideoRepository.find(video.videoId)!!.distributionMethods).isEqualTo(
            setOf(
                DistributionMethod.DOWNLOAD,
                DistributionMethod.STREAM
            )
        )
    }

    @Test
    fun `replaces language`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(language = Locale.TAIWAN))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceLanguage(video.videoId, Locale.GERMAN))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `replaces transcript`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(transcript = null))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTranscript(video.videoId, "bla bla bla"))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `replaces eventBus`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(transcript = null))
        val topic = Topic(
            name = "Bayesian Methods",
            language = Locale.US,
            confidence = 0.85,
            parent = Topic(name = "Statistics", confidence = 1.0, language = Locale.US, parent = null)
        )

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTopics(video.videoId, setOf(topic)))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.topics).containsExactly(topic)
    }

    @Test
    fun `replaces keywords`() {
        val video = mongoVideoRepository.create(TestFactories.createVideo(keywords = listOf("old")))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceKeywords(video.videoId, setOf("new")))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.keywords).containsExactly("new")
    }

    @Test
    fun `finds videos by content partner`() {
        val video1 =
            mongoVideoRepository.create(TestFactories.createVideo(title = "Video 1", contentPartnerName = "TED"))
        val video2 =
            mongoVideoRepository.create(TestFactories.createVideo(title = "Video 2", contentPartnerName = "TED"))
        val video3 =
            mongoVideoRepository.create(TestFactories.createVideo(title = "Video 3", contentPartnerName = "Reuters"))

        val videos = mongoVideoRepository.findByContentPartnerName(contentPartnerName = "TED")

        assertThat(videos).contains(video1)
        assertThat(videos).contains(video2)
        assertThat(videos).doesNotContain(video3)
    }

    @Test
    fun `find videos by content partner youtube id`() {
        val contentPartnerId = ObjectId().toHexString()
        val video1 =
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    title = "Video 1",
                    contentPartnerId = ContentPartnerId(value = contentPartnerId)
                )
            )

        val video2 =
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    title = "Video 2",
                    contentPartnerId = ContentPartnerId(value = contentPartnerId)
                )
            )

        mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "Video 3",
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString())
            )
        )

        val videos =
            mongoVideoRepository.findByContentPartnerId(contentPartnerId = ContentPartnerId(
                value = contentPartnerId
            )
            )

        assertThat(videos).containsExactly(video1, video2)
    }

    @Test
    fun `find videos by content partner id`() {
        val id = ObjectId.get().toHexString()
        val video1 =
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    title = "Video 1",
                    contentPartnerId = ContentPartnerId(id)
                )
            )
        val video2 =
            mongoVideoRepository.create(
                TestFactories.createVideo(
                    title = "Video 2",
                    contentPartnerId = ContentPartnerId(id)
                )
            )

        val id2 = ObjectId.get().toHexString()
        mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "Video 3",
                contentPartnerId = ContentPartnerId(id2)
            )
        )

        val videos = mongoVideoRepository.findByContentPartnerId(contentPartnerId = ContentPartnerId(
            id
        )
        )

        assertThat(videos).containsExactly(video1, video2)
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
                            "source", Document()
                                .append(
                                    "contentPartner", Document()
                                        .append("name", "cp-name")
                                        .append("_id", ObjectId())
                                )
                                .append("videoReference", "ref")
                        )
                        .append("playback", Document().append("id", "some-id").append("type", "KALTURA"))
                        .append("legacy", Document().append("type", LegacyVideoType.NEWS.name))
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
                            "source", Document()
                                .append(
                                    "contentPartner", Document()
                                        .append("name", "cp-name")
                                        .append("_id", ObjectId())
                                )
                                .append("videoReference", "ref")
                        )
                        .append(
                            "playback", Document()
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
                        .append("legacy", Document().append("type", LegacyVideoType.NEWS.name))
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
