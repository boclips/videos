package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.infrastructure.DATABASE_NAME
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AttachmentFactory
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
    fun `update playback`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
                playback = createKalturaPlayback(entryId = "original-entry")
            )
        )

        mongoVideoRepository.bulkUpdate(
            listOf(
                VideoUpdateCommand.ReplacePlayback(
                    originalAsset.videoId,
                    createKalturaPlayback(
                        duration = Duration.ZERO,
                        entryId = "new-entry",
                        downloadUrl = "download-url-updated"
                    )
                )
            )
        )

        val updatedAsset = mongoVideoRepository.find(originalAsset.videoId)

        assertThat(updatedAsset!!.playback).isNotNull
        assertThat(updatedAsset.playback.id.value).isEqualTo("new-entry")
        assertThat(updatedAsset.playback.duration).isEqualTo(Duration.ZERO)
        assertThat((updatedAsset.playback as StreamPlayback).downloadUrl).isEqualTo("download-url-updated")
    }

    @Test
    fun `update title`() {
        val originalAsset = mongoVideoRepository.create(createVideo(title = "old title"))

        val updatedAsset =
            mongoVideoRepository.update(VideoUpdateCommand.ReplaceTitle(originalAsset.videoId, "new title"))

        assertThat(updatedAsset.title).isEqualTo("new title")
    }

    @Test
    fun `update description`() {
        val originalAsset = mongoVideoRepository.create(createVideo(description = "old description"))

        val updatedAsset =
            mongoVideoRepository.update(VideoUpdateCommand.ReplaceDescription(originalAsset.videoId, "new description"))

        assertThat(updatedAsset.description).isEqualTo("new description")
    }

    @Test
    fun `update subjects`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
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
        assertThat(updatedAsset.subjects.items).containsOnly(biology)
    }

    @Test
    fun `remove subject`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
                title = "original title",
                subjects = setOf(maths)
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.RemoveSubject(
                videoId = originalAsset.videoId,
                subjectId = maths.id
            )
        )

        assertThat(updatedAsset.subjects.items).isEmpty()
    }

    @Test
    fun `update setManually`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
                title = "original title",
                subjects = emptySet()
            )
        )

        assertThat(originalAsset.subjects.setManually).isNull()

        val afterStep1 = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceSubjectsWereSetManually(originalAsset.videoId, true)
        )

        assertThat(afterStep1).isEqualToIgnoringGivenFields(originalAsset, "subjects")
        assertThat(afterStep1.subjects.setManually).isTrue()

        val afterStep2 = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceSubjectsWereSetManually(originalAsset.videoId, false)
        )

        assertThat(afterStep2).isEqualToIgnoringGivenFields(originalAsset, "subjects")
        assertThat(afterStep2.subjects.setManually).isFalse()
    }

    @Test
    fun `update age range`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
                title = "original title"
            )
        )

        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceAgeRange(
                originalAsset.videoId,
                AgeRange.of(min = 3, max = 5, curatedManually = true)
            )
        )

        assertThat(updatedAsset.ageRange).isEqualTo(AgeRange.of(min = 3, max = 5, curatedManually = true))
    }

    @Test
    fun `update user rating`() {
        val originalAsset = mongoVideoRepository.create(
            createVideo(
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
        val originalAsset = mongoVideoRepository.create(createVideo())
        val tag = TestFactories.createUserTag(label = "Alex", userId = "user-1")
        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceTag(
                originalAsset.videoId, tag

            )
        )
        assertThat(updatedAsset.tags.first().tag.label).isEqualTo("Alex")
    }

    @Test
    fun `update promoted`() {
        val originalAsset = mongoVideoRepository.create(createVideo())
        val updatedAsset = mongoVideoRepository.update(
            VideoUpdateCommand.ReplacePromoted(
                originalAsset.videoId, true

            )
        )

        assertThat(updatedAsset.promoted).isEqualTo(true)
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
            createVideo(
                title = "original title 1",
                playback = createKalturaPlayback(
                    duration = Duration.ofMinutes(1)
                ),
                subjects = setOf(maths)
            )
        )

        val originalVideo2 = mongoVideoRepository.create(
            createVideo(
                title = "original title 2",
                playback = createKalturaPlayback(
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
            ),
            VideoUpdateCommand.ReplacePromoted(
                videoId = originalVideo2.videoId,
                promoted = true
            ),
            VideoUpdateCommand.ReplaceAdditionalDescription(
                videoId = originalVideo2.videoId,
                additionalDescription = "edited additional description"
            ),
            VideoUpdateCommand.ReplaceCustomThumbnail(
                videoId = originalVideo2.videoId,
                customThumbnail = true
            )
        )

        val updatedVideos = mongoVideoRepository.bulkUpdate(updates)

        val updatedVideo1 = updatedVideos.find { it.videoId == originalVideo1.videoId }!!
        val updatedVideo2 = updatedVideos.find { it.videoId == originalVideo2.videoId }!!
        assertThat(updatedVideo1).isEqualToIgnoringGivenFields(originalVideo1, "subjects", "duration", "playback")
        assertThat(updatedVideo1.playback.duration).isEqualTo(Duration.ofMinutes(10))
        assertThat(updatedVideo1.subjects.items).isEmpty()

        assertThat(updatedVideo2).isEqualToIgnoringGivenFields(
            originalVideo2,
            "subjects",
            "duration",
            "playback",
            "promoted",
            "additionalDescription"
        )
        assertThat(updatedVideo2.playback.duration).isEqualTo(Duration.ofMinutes(11))
        assertThat(updatedVideo2.playback).isInstanceOf(StreamPlayback::class.java)
        assertThat((updatedVideo2.playback as StreamPlayback).customThumbnail).isTrue()
        assertThat(updatedVideo2.subjects.items).isEqualTo(setOf(biology))
        assertThat(updatedVideo2.promoted).isTrue()
        assertThat(updatedVideo2.additionalDescription).isEqualTo("edited additional description")
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
    fun `replaces language`() {
        val video = mongoVideoRepository.create(createVideo(language = Locale.TAIWAN))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceLanguage(video.videoId, Locale.GERMAN))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `replaces transcript`() {
        val video = mongoVideoRepository.create(createVideo(transcript = null))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTranscript(video.videoId, "bla bla bla"))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `replaces eventBus`() {
        val video = mongoVideoRepository.create(createVideo(transcript = null))
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
        val video = mongoVideoRepository.create(createVideo(keywords = listOf("old")))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceKeywords(video.videoId, setOf("new")))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.keywords).containsExactly("new")
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

    @Test
    fun `add attachment to video`() {
        val video = mongoVideoRepository.create(createVideo(attachments = emptyList()))

        val updatedVideo = mongoVideoRepository.update(
            VideoUpdateCommand.ReplaceAttachments(
                videoId = video.videoId,
                attachments = listOf(
                    AttachmentFactory.sample(
                        description = "description",
                        type = AttachmentType.ACTIVITY,
                        linkToResource = "some-link"
                    )
                )
            )
        )

        assertThat(updatedVideo.attachments).hasSize(1)
        assertThat(updatedVideo.attachments[0].attachmentId).isNotNull
        assertThat(updatedVideo.attachments[0].description).isEqualTo("description")
        assertThat(updatedVideo.attachments[0].linkToResource).isEqualTo("some-link")
        assertThat(updatedVideo.attachments[0].type).isEqualTo(AttachmentType.ACTIVITY)
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
                                    "channel", Document()
                                        .append("name", "cp-name")
                                        .append("_id", ObjectId())
                                )
                                .append("videoReference", "ref")
                        )
                        .append("playback", Document().append("id", "some-id").append("type", "KALTURA"))
                        .append("contentType", ContentType.NEWS.name)
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
                                    "channel", Document()
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
                        .append("contentType", ContentType.NEWS.name)
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
