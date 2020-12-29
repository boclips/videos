package com.boclips.videos.service

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.TestTokenFactory
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import feign.okhttp.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Collectors

class VideoServiceClientE2ETest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class Videos {
        @Test
        fun `fetch, update and delete a video`() {
            val videosClient = VideosClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "the@owner.com",
                    UserRoles.INSERT_VIDEOS,
                    UserRoles.UPDATE_VIDEOS,
                    UserRoles.VIEW_VIDEOS,
                    UserRoles.REMOVE_VIDEOS,
                    UserRoles.SHARE_VIDEOS,
                    UserRoles.RATE_VIDEOS,
                    UserRoles.DOWNLOAD_TRANSCRIPT
                ),
                feignClient = OkHttpClient()
            )

            val oldAgeRange = saveAgeRange(min = 12, max = 16, id = "12to16", label = "12-16")

            createMediaEntry(id = "123")
            val contentPartner = saveChannel(ageRanges = listOf(oldAgeRange.id.value))

            assertThrows<Exception> {
                videosClient.probeVideoReference(
                    channelId = contentPartner.id.value,
                    channelVideoId = "abc"
                )
            }

            val createdVideo =
                videosClient.createVideo(
                    VideoServiceApiFactory.createCreateVideoRequest(
                        providerId = contentPartner.id.value,
                        providerVideoId = "abc",
                        playbackId = "123"
                    )
                ).id!!

            assertThat(videosClient.getVideo(createdVideo)).isNotNull
            assertDoesNotThrow {
                videosClient.probeVideoReference(
                    channelId = contentPartner.id.value,
                    channelVideoId = "abc"
                )
            }

            videosClient.updateVideo(
                createdVideo,
                VideoServiceApiFactory.createUpdateVideoRequest(
                    title = "new title",
                    ageRangeMin = 3,
                    ageRangeMax = 9,
                    additionalDescription = "updated penguin description"
                )
            )
            videosClient.updateVideoRating(createdVideo, 4)

            val updatedVideo = videosClient.getVideo(createdVideo)
            assertThat(updatedVideo.title).isEqualTo("new title")
            assertThat(updatedVideo.yourRating).isEqualTo(4.0)
            assertThat(updatedVideo.ageRange!!.min).isEqualTo(3)
            assertThat(updatedVideo.ageRange!!.max).isEqualTo(9)
            assertThat(updatedVideo.ageRange!!.getLabel()).isEqualTo("3-9")
            assertThat(updatedVideo.playback).isNotNull
            assertThat(updatedVideo.playback!!.id).isNotNull()
            assertThat(updatedVideo.additionalDescription).isEqualTo("updated penguin description")
            assertThat(updatedVideo._links?.get("self")?.href).isNotEmpty()

            videosClient.deleteVideo(createdVideo)
            assertThrows<Exception> {
                videosClient.deleteVideo(createdVideo)
            }
        }

        @Test
        fun `search videos with filters`() {
            val videosClient = VideosClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "the@owner.com",
                    UserRoles.INSERT_VIDEOS,
                    UserRoles.UPDATE_VIDEOS,
                    UserRoles.VIEW_VIDEOS,
                    UserRoles.REMOVE_VIDEOS,
                    UserRoles.SHARE_VIDEOS,
                    UserRoles.RATE_VIDEOS,
                    UserRoles.DOWNLOAD_TRANSCRIPT
                ),
                feignClient = OkHttpClient()
            )

            saveVideo(title = "normal video") // normal video, subjects not set manually
            val editedVideo = saveVideo(title = "other video")
            val subject = saveSubject("A Subject")

            videosClient.updateVideo(
                editedVideo.value,
                VideoServiceApiFactory.createUpdateVideoRequest(subjectIds = listOf(subject.id.value))
            )

            val allVideos = videosClient
                .searchVideos(SearchVideosRequest())
            assertThat(allVideos._embedded.videos).hasSize(2)

            val filteredVideos = videosClient
                .searchVideos(SearchVideosRequest(subjects_set_manually = true))
            assertThat(filteredVideos._embedded.videos).hasSize(1)
            assertThat(filteredVideos._embedded.videos[0].id).isEqualTo(editedVideo.value)
        }
    }

    @Nested
    inner class Collections {
        @Test
        fun `can create, fetch one and many collections`() {
            val collectionsClient = CollectionsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "the@owner.com",
                    UserRoles.VIEW_COLLECTIONS,
                    UserRoles.INSERT_COLLECTIONS,
                    UserRoles.UPDATE_COLLECTIONS,
                    UserRoles.DELETE_COLLECTIONS
                ),
                feignClient = OkHttpClient()
            )

            val savedVideoId = saveVideo()

            val aCollection =
                collectionsClient.create(
                    CreateCollectionRequest(
                        title = "collection title",
                        discoverable = true,
                        videos = listOf(savedVideoId.value)
                    )
                )

            val retrievedCollection = collectionsClient.getCollection(collectionId = aCollection.id!!)
            assertThat(retrievedCollection).isNotNull
            assertThat(retrievedCollection.attachments).isEmpty()

            collectionsClient.update(
                collectionId = aCollection.id!!,
                update = UpdateCollectionRequest(
                    attachment = AttachmentRequest(
                        linkToResource = "some-link",
                        description = "some description",
                        type = "LESSON_PLAN"
                    )
                )
            )

            val updatedCollection = collectionsClient.getCollection(collectionId = aCollection.id!!)
            assertThat(updatedCollection).isNotNull
            assertThat(updatedCollection.attachments).isNotEmpty()

            assertThat(collectionsClient.getCollections()._embedded.collections).hasSize(1)

            collectionsClient.delete(aCollection.id!!)

            assertThat(collectionsClient.getCollections()._embedded.collections).isEmpty()
        }
    }

    @Nested
    inner class Subjects {
        @Test
        fun `create, list, update and delete subjects`() {
            val subjectsClient = SubjectsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "sombody@world.com",
                    UserRoles.CREATE_SUBJECTS,
                    UserRoles.DELETE_SUBJECTS,
                    UserRoles.UPDATE_SUBJECTS
                ),
                feignClient = OkHttpClient()
            )

            subjectsClient.create(CreateSubjectRequest(name = "Maths"))
            subjectsClient.create(CreateSubjectRequest(name = "French"))

            val initialSubjects = subjectsClient.getSubjects()._embedded.subjects.stream().collect(Collectors.toList())
            assertThat(initialSubjects).hasSize(2)

            val firstSubject = initialSubjects[0] as SubjectResource
            val secondSubject = (initialSubjects[1] as SubjectResource).id

            subjectsClient.deleteSubject(firstSubject.id)
            subjectsClient.updateSubject(secondSubject, CreateSubjectRequest(name = "German"))

            val updatedSubjects = subjectsClient.getSubjects()._embedded.subjects.stream().collect(Collectors.toList())
            assertThat(updatedSubjects).hasSize(1)
            assertThat((updatedSubjects[0] as SubjectResource).name).isEqualTo("German")
        }
    }

    @Nested
    inner class Channels {
        @Test
        fun `create and filters channels`() {
            val channelsClient = ChannelsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "sombody@world.com",
                    UserRoles.INSERT_CHANNELS,
                    UserRoles.VIEW_CHANNELS,
                    UserRoles.HQ
                ),
                feignClient = OkHttpClient()
            )

            channelsClient.create(
                VideoServiceApiFactory.createChannelRequest(
                    name = "TED"
                )
            )
            channelsClient.create(
                VideoServiceApiFactory.createChannelRequest(
                    name = "other channel"
                )
            )

            val channels = channelsClient.getChannels()._embedded.channels

            val channelId =
                channelsClient.getChannels()._embedded.channels.toList().first().id
            assertThat(channelsClient.getChannel(channelId = channelId)).isNotNull

            assertThat(channels).hasSize(2)
            assertThat(channels.map { it.name }).containsExactlyInAnyOrder("TED", "other channel")

            val namedChannels = channelsClient.getChannels(
                channelFilterRequest = VideoServiceApiFactory.channelFilterRequest(name = "other channel")
            )._embedded.channels
            assertThat(namedChannels).hasSize(1)
            assertThat(namedChannels.first().name).isEqualTo("other channel")
        }
    }
}
