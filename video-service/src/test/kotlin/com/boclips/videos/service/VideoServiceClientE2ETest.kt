package com.boclips.videos.service

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.httpclient.ContentPartnersClient
import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.TestTokenFactory
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.video.SearchVideosRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
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
                )
            )

            val oldAgeRange = saveAgeRange(min = 12, max = 16, id = "12to16", label = "12-16")

            createMediaEntry(id = "123")
            val contentPartner = saveContentPartner(ageRanges = listOf(oldAgeRange.id.value))

            assertThrows<Exception> {
                videosClient.probeVideoReference(
                    contentPartnerId = contentPartner.contentPartnerId.value,
                    contentPartnerVideoId = "abc"
                )
            }

            val createdVideo =
                videosClient.createVideo(
                    VideoServiceApiFactory.createCreateVideoRequest(
                        providerId = contentPartner.contentPartnerId.value,
                        providerVideoId = "abc",
                        playbackId = "123"
                    )
                ).id!!

            assertThat(videosClient.getVideo(createdVideo)).isNotNull
            assertDoesNotThrow {
                videosClient.probeVideoReference(
                    contentPartnerId = contentPartner.contentPartnerId.value,
                    contentPartnerVideoId = "abc"
                )
            }

            videosClient.updateVideo(
                createdVideo,
                VideoServiceApiFactory.createUpdateVideoRequest(
                    title = "new title",
                    ageRangeMin = 3,
                    ageRangeMax = 9
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
                )
            )

            saveVideo() // normal video, subjects not set manually
            val editedVideo = saveVideo()
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
        fun `can fetch one and many collections, including details`() {
            val savedVideoId = saveVideo()
            val savedCollectionId = saveCollection(owner = "the@owner.com", curated = true)
            val collectionWithVideos =
                saveCollection(owner = "another@owner.com", curated = true, videos = listOf(savedVideoId.value))

            val collectionsClient = CollectionsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory("the@owner.com", UserRoles.VIEW_COLLECTIONS)
            )

            assertThat(collectionsClient.getCollection(collectionId = savedCollectionId.value)).isNotNull

            val detailedCollection = collectionsClient.getCollection(collectionWithVideos.value, Projection.details)
            assertThat(detailedCollection.videos.first().title).isNotBlank()

            assertThat(collectionsClient.getCollections()._embedded.collections).hasSize(2)

            val collections = collectionsClient.getCollections(
                CollectionFilterRequest(owner = "another@owner.com", public = true, projection = Projection.details)
            )._embedded.collections

            assertThat(collections).hasSize(1)
            assertThat(collections[0].title).isNotNull()
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
                )
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
    inner class ContentPartners {
        @Test
        fun `create and filters content partners`() {
            val contentPartnersClient = ContentPartnersClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory(
                    "sombody@world.com",
                    UserRoles.INSERT_CONTENT_PARTNERS,
                    UserRoles.VIEW_CONTENT_PARTNERS,
                    UserRoles.BACKOFFICE
                )
            )

            contentPartnersClient.create(
                VideoServiceApiFactory.createContentPartnerRequest(
                    name = "TED",
                    accreditedToYtChannel = null
                )
            )
            contentPartnersClient.create(
                VideoServiceApiFactory.createContentPartnerRequest(
                    name = "YoutubeChannel",
                    accreditedToYtChannel = "YT-123"
                )
            )

            val contentPartners = contentPartnersClient.getContentPartners()._embedded.contentPartners

            val contentPartnerId =
                contentPartnersClient.getContentPartners()._embedded.contentPartners.toList().first().id
            assertThat(contentPartnersClient.getContentPartner(contentPartnerId = contentPartnerId)).isNotNull

            assertThat(contentPartners).hasSize(2)
            assertThat(contentPartners.map { it.name }).containsExactlyInAnyOrder("TED", "YoutubeChannel")

            val officialContentPartners = contentPartnersClient.getContentPartners(
                contentPartnerFilterRequest = VideoServiceApiFactory.contentPartnerFilterRequest(official = true)
            )._embedded.contentPartners
            assertThat(officialContentPartners).hasSize(1)
            assertThat(officialContentPartners.first().name).isEqualTo("TED")

            val namedContentPartners = contentPartnersClient.getContentPartners(
                contentPartnerFilterRequest = VideoServiceApiFactory.contentPartnerFilterRequest(name = "YoutubeChannel")
            )._embedded.contentPartners
            assertThat(namedContentPartners).hasSize(1)
            assertThat(namedContentPartners.first().name).isEqualTo("YoutubeChannel")

            val accreditedToContentPartners = contentPartnersClient.getContentPartners(
                contentPartnerFilterRequest = VideoServiceApiFactory.contentPartnerFilterRequest(accreditedToYtChannel = "YT-123")
            )._embedded.contentPartners
            assertThat(accreditedToContentPartners).hasSize(1)
            assertThat(accreditedToContentPartners.first().name).isEqualTo("YoutubeChannel")
        }
    }
}
