package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.attachment.AttachmentId
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.UserContractService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.attachments.AttachmentToResourceConverter
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory
import com.boclips.videos.service.presentation.hateoas.AttachmentsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectToResourceConverter
import com.boclips.videos.service.presentation.video.PlaybackToResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCollectionsTest {

    lateinit var playbackToResourceConverter: PlaybackToResourceConverter
    lateinit var collectionResourceFactory: CollectionResourceFactory
    lateinit var videoService: VideoService
    var collectionRepository: CollectionRepository = mock()
    var collectionService: CollectionService = mock()
    lateinit var videosLinkBuilder: VideosLinkBuilder
    lateinit var attachmentsLinkBuilder: AttachmentsLinkBuilder

    val getContractedCollections: GetContractedCollections = mock()
    val userContractService: UserContractService = mock()

    val video = TestFactories.createVideo()

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        videoService = mock {
            on { getPlayableVideo(any<List<VideoId>>()) } doReturn listOf(
                TestFactories.createVideo()
            )
        }
        videosLinkBuilder = mock()
        attachmentsLinkBuilder = mock()
        playbackToResourceConverter = mock()
        collectionResourceFactory =
            CollectionResourceFactory(
                VideoToResourceConverter(videosLinkBuilder, playbackToResourceConverter),
                SubjectToResourceConverter(),
                AttachmentToResourceConverter(attachmentsLinkBuilder),
                videoService
            )
    }

    @Test
    fun `fetches all bookmarked collections with skinny videos`() {
        collectionRepository = mock {
            on {
                getBookmarkedByUser(
                    PageRequest(0, 1),
                    UserId("me@me.com")
                )
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true,
                        bookmarks = setOf(UserId("me@me.com"))
                    ),
                    TestFactories.createCollection(
                        isPublic = true,
                        bookmarks = setOf(UserId("me@me.com"))
                    )
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.BOOKMARKED,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.list
        )

        assertThat(collections.elements).hasSize(2)
        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.public).isEqualTo(true)
    }

    @Test
    fun `fetches all public collections with skinny videos`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.list
        )

        assertThat(collections.elements).hasSize(2)
        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.public).isEqualTo(true)
    }

    @Test
    fun `fetches all public collections with fat videos`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        owner = "yoyoyo@public.com",
                        title = "collection title",
                        videos = listOf(video.videoId),
                        isPublic = true
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.details
        )

        assertThat(collections.elements).hasSize(2)

        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("yoyoyo@public.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
        assertThat(collection.public).isEqualTo(true)
        assertThat(collection.videos.first().content.title).isEqualTo(video.title)
    }

    @Test
    fun `fetches collections with their subjects`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        isPublic = true,
                        subjects = setOf(TestFactories.createSubject())
                    ),
                    TestFactories.createCollection(isPublic = true)
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.details
        )

        assertThat(collections.elements).hasSize(2)

        val collection = collections.elements.first()

        assertThat(collection.id).isEqualTo("collection-id")

        assertThat(collection.subjects).hasSize(1)
        assertThat(collection.subjects.first().content.id).isNotBlank()
    }

    @Test
    fun `fetches collections with an attachment`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        isPublic = true,
                        subjects = setOf(TestFactories.createSubject()),
                        attachments = setOf(
                            Attachment(
                                attachmentId = AttachmentId("id"),
                                description = "Description",
                                type = AttachmentType.LESSON_PLAN,
                                linkToResource = "https://example.com/download"
                            )
                        )
                    )
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.details
        )

        assertThat(collections.elements).hasSize(1)

        val collection = collections.elements.first()

        assertThat(collection.id).isEqualTo("collection-id")

        assertThat(collection.attachments).hasSize(1)

        val attachment = collection.attachments!!.first()
        assertThat(attachment.content.id).isEqualTo("id")
        assertThat(attachment.content.description).isEqualTo("Description")
        assertThat(attachment.content.type).isEqualTo("LESSON_PLAN")
    }

    @Test
    fun `fetches collections without an attachment`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn Page(
                listOf(
                    TestFactories.createCollection(
                        id = CollectionId("collection-id"),
                        isPublic = true,
                        subjects = setOf(TestFactories.createSubject())
                    )
                ), PageInfo(true, 1001)
            )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1,
                subjects = emptyList()
            ),
            projection = Projection.details
        )

        assertThat(collections.elements).hasSize(1)

        val collection = collections.elements.first()

        assertThat(collection.id).isEqualTo("collection-id")

        assertThat(collection.attachments).hasSize(0)
    }

    @Test
    fun `searches collections by query`() {
        collectionService = mock {
            on {
                search(any())
            } doReturn
                Page(
                    listOf(
                        TestFactories.createCollection(
                            id = CollectionId("collection-id"),
                            owner = "yoyoyo@public.com",
                            title = "collection title",
                            videos = listOf(video.videoId),
                            isPublic = true,
                            subjects = setOf(TestFactories.createSubject())
                        )
                    ), PageInfo(false, 1)
                )
        }

        val collections = GetCollections(
            collectionService,
            collectionResourceFactory,
            getContractedCollections,
            userContractService,
            GetBookmarkedCollections(collectionRepository),
            CollectionQueryAssembler()
        ).invoke(
            CollectionFilter(
                query = "title",
                visibility = CollectionFilter.Visibility.PUBLIC,
                pageNumber = 0,
                pageSize = 1
            ),
            projection = Projection.list
        )

        assertThat(collections.elements).hasSize(1)
        val collection = collections.elements.first()
        assertThat(collection.id).isEqualTo("collection-id")
    }
}
