package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.StreamPlayback
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.Voice
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AttachmentFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createKalturaPlayback
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Locale

class MongoVideoRepositoryUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    val maths = TestFactories.createSubject(name = "Maths")
    val biology = TestFactories.createSubject(name = "Biology")

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

    @Test
    fun `replaces language`() {
        val video = mongoVideoRepository.create(
            createVideo(
                voice = Voice.UnknownVoice(
                    language = Locale.TAIWAN,
                    transcript = null
                )
            )
        )

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceLanguage(video.videoId, Locale.GERMAN))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.voice.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `replaces transcript`() {
        val video =
            mongoVideoRepository.create(createVideo(voice = Voice.UnknownVoice(language = null, transcript = null)))

        mongoVideoRepository.update(VideoUpdateCommand.ReplaceTranscript(video.videoId, "bla bla bla"))

        val updatedAsset = mongoVideoRepository.find(video.videoId)

        assertThat(updatedAsset!!.voice.transcript).isEqualTo("bla bla bla")
    }

    @Test
    fun `replaces topics`() {
        val video =
            mongoVideoRepository.create(createVideo(voice = Voice.UnknownVoice(language = null, transcript = null)))
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

    @Nested
    inner class CategoryUpdates {
        @Test
        fun `update categories with channel source`() {
            val originalAsset = mongoVideoRepository.create(createVideo(title = "old title"))

            val updatedAsset =
                mongoVideoRepository.update(
                    VideoUpdateCommand.ReplaceCategories(
                        videoId = originalAsset.videoId,
                        categories = setOf(CategoryWithAncestors(codeValue = CategoryCode("M"), description = "music")),
                        source = CategorySource.CHANNEL
                    )
                )

            assertThat(updatedAsset.channelCategories).containsOnly(
                CategoryWithAncestors(codeValue = CategoryCode("M"), description = "music")
            )
        }

        @Test
        fun `update categories with video source`() {
            val originalAsset = mongoVideoRepository.create(createVideo(title = "old title"))

            val updatedAsset =
                mongoVideoRepository.update(
                    VideoUpdateCommand.ReplaceCategories(
                        videoId = originalAsset.videoId,
                        categories = setOf(CategoryWithAncestors(codeValue = CategoryCode("M"), description = "music")),
                        source = CategorySource.MANUAL
                    )
                )

            assertThat(updatedAsset.manualCategories).containsOnly(
                CategoryWithAncestors(codeValue = CategoryCode("M"), description = "music")
            )
        }

        @Test
        fun `add categories to video`() {
            val originalVideo = mongoVideoRepository.create(
                createVideo(
                    title = "old title", categories = mapOf(
                        CategorySource.MANUAL to setOf(
                            CategoryWithAncestors(
                                codeValue = CategoryCode("A"),
                                description = "A CAT"
                            )
                        )
                    )
                )
            )

            val updatedVideo = mongoVideoRepository.update(
                VideoUpdateCommand.AddCategories(
                    videoId = originalVideo.videoId,
                    categories = setOf(
                        CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT")
                    ),
                    source = CategorySource.MANUAL
                )
            )

            assertThat(updatedVideo.manualCategories).containsExactlyInAnyOrder(
                CategoryWithAncestors(codeValue = CategoryCode("A"), description = "A CAT"),
                CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT")
            )
        }

        @Test
        fun `add only unique categories to video`() {
            val originalVideo = mongoVideoRepository.create(
                createVideo(
                    title = "old title", categories = mapOf(
                        CategorySource.MANUAL to setOf(
                            CategoryWithAncestors(
                                codeValue = CategoryCode("A"),
                                description = "A CAT"
                            ),
                            CategoryWithAncestors(
                                codeValue = CategoryCode("B"),
                                description = "B CAT"
                            )
                        )
                    )
                )
            )

            val updatedVideo = mongoVideoRepository.update(
                VideoUpdateCommand.AddCategories(
                    videoId = originalVideo.videoId,
                    categories = setOf(
                        CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT"),
                        CategoryWithAncestors(codeValue = CategoryCode("C"), description = "C CAT"),
                    ),
                    source = CategorySource.MANUAL
                )
            )

            assertThat(updatedVideo.manualCategories).containsExactlyInAnyOrder(
                CategoryWithAncestors(codeValue = CategoryCode("A"), description = "A CAT"),
                CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT"),
                CategoryWithAncestors(codeValue = CategoryCode("C"), description = "C CAT"),
            )
        }

        @Test
        fun `add multiple categories to video`() {
            val originalVideo = mongoVideoRepository.create(
                createVideo(
                    title = "old title", categories = mapOf(
                        CategorySource.MANUAL to setOf(
                            CategoryWithAncestors(
                                codeValue = CategoryCode("A"),
                                description = "A CAT"
                            )
                        )
                    )
                )
            )

            val updatedVideo = mongoVideoRepository.update(
                VideoUpdateCommand.AddCategories(
                    videoId = originalVideo.videoId,
                    categories = setOf(
                        CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT"),
                        CategoryWithAncestors(codeValue = CategoryCode("C"), description = "C CAT"),
                    ),
                    source = CategorySource.MANUAL
                )
            )

            assertThat(updatedVideo.manualCategories).containsExactlyInAnyOrder(
                CategoryWithAncestors(codeValue = CategoryCode("A"), description = "A CAT"),
                CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B CAT"),
                CategoryWithAncestors(codeValue = CategoryCode("C"), description = "C CAT"),
            )
        }
    }
}
