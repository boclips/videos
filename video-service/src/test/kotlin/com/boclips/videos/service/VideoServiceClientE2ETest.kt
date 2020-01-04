package com.boclips.videos.service

import com.boclips.videos.api.httpclient.CollectionsClient
import com.boclips.videos.api.httpclient.SubjectsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.TestTokenFactory
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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

            val createdVideo = saveVideo()

            assertThat(videosClient.getVideo(createdVideo.value)).isNotNull

            videosClient.updateVideo(
                createdVideo.value,
                VideoServiceApiFactory.createUpdateVideoRequest(title = "new title")
            )
            videosClient.updateVideoRating(createdVideo.value, 4)

            assertThat(videosClient.getVideo(createdVideo.value).content.title).isEqualTo("new title")
            assertThat(videosClient.getVideo(createdVideo.value).content.yourRating).isEqualTo(4.0)

            videosClient.deleteVideo(createdVideo.value)
            assertThrows<Exception> {
                videosClient.deleteVideo(createdVideo.value)
            }
        }
    }

    @Nested
    inner class Collections {
        @Test
        fun `can fetch a collection`() {
            val savedCollection = saveCollection(owner = "the@owner.com")

            val collectionsClient = CollectionsClient.create(
                apiUrl = "http://localhost:$randomServerPort",
                objectMapper = objectMapper,
                tokenFactory = TestTokenFactory("the@owner.com", UserRoles.VIEW_COLLECTIONS)
            )

            assertThat(collectionsClient.getCollection(savedCollection.value)).isNotNull
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

            val initialSubjects = subjectsClient.getSubjects().content.stream().collect(Collectors.toList())
            assertThat(initialSubjects).hasSize(2)

            val firstSubject = initialSubjects[0] as SubjectResource
            val secondSubject = (initialSubjects[1] as SubjectResource).id

            subjectsClient.deleteSubject(firstSubject.id)
            subjectsClient.updateSubject(secondSubject, CreateSubjectRequest(name = "German"))

            val updatedSubjects = subjectsClient.getSubjects().content.stream().collect(Collectors.toList())
            assertThat(updatedSubjects).hasSize(1)
            assertThat((updatedSubjects[0] as SubjectResource).name).isEqualTo("German")
        }
    }
}
