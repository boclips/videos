package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.collection.CollectionResource
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.EventController
import com.boclips.videos.service.presentation.projections.Projection
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class CollectionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val LOG_COLLECTION_INTERACTION = "interactedWith"
    }

    fun collection(id: String?) = getIfHasRole(UserRoles.VIEW_COLLECTIONS) { collectionResourceLink(id, "collection") }

    fun editCollection(collectionResource: CollectionResource) =
        collectionResource.mine?.let { if (it) collectionResourceLink(collectionResource.id, "edit") else null }

    fun removeCollection(collectionResource: CollectionResource) =
        collectionResource.mine?.let { if (it) collectionResourceLink(collectionResource.id, "remove") else null }

    fun addVideoToCollection(collectionResource: CollectionResource) =
        collectionResource.mine?.let {
            if (it) {
                Link(
                    getCollectionsRoot()
                        .pathSegment(collectionResource.id)
                        .pathSegment("videos")
                        .toUriString() + "/{video_id}", "addVideo"
                )
            } else null
        }

    fun removeVideoFromCollection(collectionResource: CollectionResource) =
        collectionResource.mine?.let {
            if (it) {
                Link(
                    getCollectionsRoot()
                        .pathSegment(collectionResource.id)
                        .pathSegment("videos")
                        .toUriString() + "/{video_id}", "removeVideo"
                )
            } else null
        }

    private fun collectionResourceLink(id: String?, rel: String): Link {
        return if (id == null) {
            Link(
                getCollectionsRoot().toUriString() + "/{id}",
                rel
            )
        } else {
            Link(
                getCollectionsRoot()
                    .pathSegment(id)
                    .toUriString(), rel
            )
        }
    }

    fun createCollection() =
        getIfHasRole(UserRoles.INSERT_COLLECTIONS) { Link(getCollectionsRoot().toUriString(), "createCollection") }

    fun publicCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ) = Link(
        getCollectionsRoot()
            .queryParam("projection", projection)
            .queryParam("public", true)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString(), "publicCollections"
    )

    fun searchPublicCollections(
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ) = getIfHasRole(UserRoles.VIEW_COLLECTIONS) {
        Link(
            getCollectionsRoot()
                .queryParam("public", true)
                .toUriString() + "{&query,subject,projection,page,size}", "searchPublicCollections"
        )
    }

    fun adminCollectionSearch() = getIfHasRole(UserRoles.VIEW_ANY_COLLECTION) {
        Link(
            getCollectionsRoot()
                .toUriString() + "{?query,subject,projection,page,size}",
            "adminCollectionSearch"
        )
    }

    fun searchCollections() =
        getIfHasRole(UserRoles.VIEW_COLLECTIONS) {
            Link(
                getCollectionsRoot()
                    .toUriString() + "{?query,subject,public,projection,page,size}",
                "searchCollections"
            )
        }

    fun bookmarkedCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ) = getIfHasRole(UserRoles.VIEW_COLLECTIONS) {
        Link(
            getCollectionsRoot()
                .queryParam("projection", projection)
                .queryParam("public", true)
                .queryParam("bookmarked", true)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString(), "bookmarkedCollections"
        )
    }

    fun myCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ) = getIfHasRole(UserRoles.VIEW_COLLECTIONS) { currentUser ->
        Link(
            collectionsLink(projection = projection, page = page, size = size)
                .queryParam("owner", currentUser)
                .toUriString(),
            "myCollections"
        )
    }

    fun collectionsByOwner(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ) = getIfHasRole(UserRoles.VIEW_ANY_COLLECTION) {
        Link(
            collectionsLink(projection = projection, page = page, size = size)
                .toUriString(),
            "collectionsByOwner"
        )
    }

    private fun collectionsLink(projection: Projection, page: Int, size: Int) = getCollectionsRoot()
        .queryParam("projection", projection)
        .queryParam("page", page)
        .queryParam("size", size)

    fun self(collectionResource: CollectionResource? = null): Link {
        if (collectionResource != null) {
            return collectionResourceLink(collectionResource.id, "self")
        }
        return Link(uriComponentsBuilderFactory.getInstance().toUriString())
    }

    fun next(pageInfo: PageInfo): Link? {
        return if (pageInfo.hasMoreElements) {
            val currentPage =
                uriComponentsBuilderFactory.getInstance().build().queryParams["page"]?.first()?.toInt() ?: 0

            Link(
                uriComponentsBuilderFactory.getInstance().replaceQueryParam("page", currentPage + 1).toUriString(),
                "next"
            )
        } else {
            null
        }
    }

    fun bookmark(collectionResource: CollectionResource) =
        if (collectionResource.bookmarked == null || collectionResource.mine == null || collectionResource.public == null) {
            null
        } else if (collectionResource.bookmarked!! || collectionResource.mine!! || !collectionResource.public!!) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collectionResource.id)
                .queryParam("bookmarked", "true")
                .toUriString()
            Link(href, "bookmark")
        }

    fun unbookmark(collectionResource: CollectionResource) =
        if (collectionResource.bookmarked == null || collectionResource.mine == null || collectionResource.public == null) {
            null
        } else if (!collectionResource.bookmarked!! || collectionResource.mine!! || !collectionResource.public!!) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collectionResource.id)
                .queryParam("bookmarked", "false")
                .toUriString()
            Link(href, "unbookmark")
        }

    fun interactedWith(collectionResource: CollectionResource) = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java)
            .logCollectionInteractedWithEvent(collectionId = collectionResource.id!!, data = null)
    ).withRel(Rels.LOG_COLLECTION_INTERACTION)

    fun projections() =
        ProjectionsCollectionsLinkBuilder(
            uriComponentsBuilderFactory
        )

    private fun getCollectionsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/collections")
        .replaceQueryParams(null)

    class ProjectionsCollectionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
        fun list() =
            Link(
                uriComponentsBuilderFactory.getInstance().replaceQueryParam("projection", "list").toUriString(),
                "list"
            )

        fun details() =
            Link(
                uriComponentsBuilderFactory.getInstance().replaceQueryParam("projection", "details").toUriString(),
                "details"
            )
    }
}
