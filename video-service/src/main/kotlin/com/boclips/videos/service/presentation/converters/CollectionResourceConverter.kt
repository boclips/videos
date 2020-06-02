package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.api.response.collection.CollectionsResource
import com.boclips.videos.api.response.collection.CollectionsWrapperResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import org.springframework.hateoas.PagedModel

class CollectionResourceConverter(
    private val videoToResourceConverter: VideoToResourceConverter,
    private val attachmentsToResourceConverter: AttachmentToResourceConverter,
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val videoRetrievalService: VideoRetrievalService
) {
    fun buildCollectionResource(collection: Collection, projection: Projection, user: User): CollectionResource {
        return when (projection) {
            Projection.list -> buildCollectionListResource(collection, user)
            Projection.details, Projection.full -> buildCollectionDetailsResource(collection, user)
        }
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

    fun buildCollectionDetailsResource(collection: Collection, user: User): CollectionResource {
        return CollectionResource(
            id = collection.id.value,
            owner = collection.owner.value,
            title = collection.title,
            videos = videoToResourceConverter.convert(
                videoRetrievalService.getPlayableVideos(collection.videos, user.accessRules.videoAccess),
                user
            ),
            updatedAt = collection.updatedAt,
            discoverable = collection.discoverable,
            public = collection.discoverable,
            promoted = collection.promoted,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = collection.subjects.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = collection.attachments.map { attachmentsToResourceConverter.convert(it) }.toSet(),
            subCollections = collection.subCollections.map {
                buildCollectionDetailsResource(
                    collection = it,
                    user = user
                )
            },
            _links = generateLinks(collection, user).map { it.rel to it }.toMap()
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
            discoverable = collection.discoverable,
            public = collection.discoverable,
            promoted = collection.promoted,
            mine = collection.isOwner(user),
            bookmarked = collection.isBookmarkedBy(user),
            createdBy = collection.createdBy(),
            subjects = collection.subjects.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            ageRange = AgeRangeToResourceConverter.convert(collection.ageRange),
            description = collection.description,
            attachments = collection.attachments.map { attachmentsToResourceConverter.convert(it) }.toSet(),
            subCollections = collection.subCollections.map {
                buildCollectionListResource(
                    collection = it,
                    user = user
                )
            },
            _links = generateLinks(collection = collection, user = user).map { it.rel to it }.toMap()
        )
    }

    private fun generateLinks(
        collection: Collection,
        user: User
    ): List<HateoasLink> {
        return listOfNotNull(
            collectionsLinkBuilder.self(collection.id.value),
            if (!collection.default) {
                collectionsLinkBuilder.editCollection(collection, user)
            } else null,
            if (!collection.default) {
                collectionsLinkBuilder.removeCollection(collection, user)
            } else null,
            if (!collection.isOwner(owner = user)) {
                collectionsLinkBuilder.bookmark(collection, user)
            } else null,
            if (!collection.isOwner(owner = user)) {
                collectionsLinkBuilder.unbookmark(collection, user)
            } else null,
            collectionsLinkBuilder.addVideoToCollection(collection, user),
            collectionsLinkBuilder.removeVideoFromCollection(collection, user),
            collectionsLinkBuilder.interactedWith(collection)
        )
    }
}
