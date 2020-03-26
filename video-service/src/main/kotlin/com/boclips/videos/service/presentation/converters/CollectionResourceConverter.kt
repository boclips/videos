package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import com.boclips.videos.api.response.collection.CollectionsWrapperResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import org.springframework.hateoas.PagedModel

class CollectionResourceConverter(
    private val videoToResourceConverter: VideoToResourceConverter,
    private val attachmentsToResourceConverter: AttachmentToResourceConverter,
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val videoService: VideoService
) {
    fun buildCollectionResource(collection: Collection, projection: Projection, user: User) =
        when (projection) {
            Projection.list -> buildCollectionListResource(collection, user)
            Projection.details -> buildCollectionDetailsResource(collection, user)
        }

    fun buildCollectionDetailsResource(collection: Collection, user: User): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(
                videoService.getPlayableVideos(collection.videos, user.accessRules.videoAccess),
                user
            ),
            updatedAt = collection.updatedAt,
            public = collection.isPublic,
            promoted = collection.promoted,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = collection.subjects.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = collection.attachments.map { attachmentsToResourceConverter.convert(it) }.toSet(),
            _links = listOfNotNull(
                collectionsLinkBuilder.self(collection.id.value),
                collectionsLinkBuilder.editCollection(collection, user),
                collectionsLinkBuilder.removeCollection(collection, user),
                collectionsLinkBuilder.addVideoToCollection(collection, user),
                collectionsLinkBuilder.removeVideoFromCollection(collection, user),
                collectionsLinkBuilder.bookmark(collection, user),
                collectionsLinkBuilder.unbookmark(collection, user),
                collectionsLinkBuilder.interactedWith(collection)
            ).map { it.rel to it }.toMap()
        )
    }

    fun buildCollectionListResource(
        collection: Collection,
        user: User
    ): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convertVideoIds(collection.videos),
            updatedAt = collection.updatedAt,
            public = collection.isPublic,
            promoted = collection.promoted,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = collection.subjects.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = collection.attachments.map { attachmentsToResourceConverter.convert(it) }.toSet(),
            _links = listOfNotNull(
                collectionsLinkBuilder.self(collection.id.value),
                collectionsLinkBuilder.editCollection(collection, user),
                collectionsLinkBuilder.removeCollection(collection, user),
                collectionsLinkBuilder.addVideoToCollection(collection, user),
                collectionsLinkBuilder.removeVideoFromCollection(collection, user),
                collectionsLinkBuilder.bookmark(collection, user),
                collectionsLinkBuilder.unbookmark(collection, user),
                collectionsLinkBuilder.interactedWith(collection)
            ).map { it.rel to it }.toMap()
        )
    }

    fun buildCollectionsResource(
        collections: ResultsPage<Collection, Nothing>,
        currentUser: User,
        projection: Projection?
    ): Any {
        return CollectionsResource(
            _embedded = CollectionsWrapperResource(collections.elements.map {
                buildCollectionResource(
                    it,
                    projection ?: Projection.list,
                    currentUser
                )
            }),
            page = PagedModel.PageMetadata(
                collections.pageInfo.pageRequest.size.toLong(),
                collections.pageInfo.pageRequest.page.toLong(),
                collections.pageInfo.totalElements,
                collections.pageInfo.totalElements / collections.pageInfo.pageRequest.size.toLong()
            ),
            _links = listOfNotNull(
                collectionsLinkBuilder.projections().list(),
                collectionsLinkBuilder.projections().details(),
                collectionsLinkBuilder.self(null),
                collectionsLinkBuilder.next(collections.pageInfo)
            ).map { it.rel to it }.toMap()
        )
    }
}
