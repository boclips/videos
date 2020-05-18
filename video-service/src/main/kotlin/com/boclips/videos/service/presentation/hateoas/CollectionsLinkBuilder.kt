package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getCurrentUserIfNotAnonymous
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class CollectionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val LOG_COLLECTION_INTERACTION = "interactedWith"
    }

    fun collection(id: String?): HateoasLink? {
        return collectionResourceLink(id, "collection")
    }

    fun editCollection(collection: Collection, user: User): HateoasLink? {
        return if (collection.isOwner(user)) collectionResourceLink(collection.id.value, "edit") else null
    }

    fun removeCollection(collection: Collection, user: User): HateoasLink? {
        return if (collection.isOwner(user)) collectionResourceLink(collection.id.value, "remove") else null
    }

    fun addVideoToCollection(collection: Collection, user: User): HateoasLink? {
        return if (collection.isOwner(user)) {
            HateoasLink.of(
                Link(
                    getCollectionsRoot()
                        .pathSegment(collection.id.value)
                        .pathSegment("videos")
                        .toUriString() + "/{video_id}", "addVideo"
                )
            )
        } else null
    }

    fun removeVideoFromCollection(collection: Collection, user: User): HateoasLink? {
        return if (collection.isOwner(user)) {
            HateoasLink.of(
                Link(
                    getCollectionsRoot()
                        .pathSegment(collection.id.value)
                        .pathSegment("videos")
                        .toUriString() + "/{video_id}", "removeVideo"
                )
            )
        } else null
    }

    fun createCollection(): HateoasLink? {
        return getIfHasRole(UserRoles.INSERT_COLLECTIONS) {
            HateoasLink.of(
                Link(
                    getCollectionsRoot().toUriString(),
                    "createCollection"
                )
            )
        }
    }

    fun searchCollections(): HateoasLink? {
        return getIfHasRole(UserRoles.VIEW_COLLECTIONS) {
            HateoasLink(
                href = getCollectionsRoot()
                    .toUriString() + "{?query,subject,discoverable,projection,page,size,age_range_min,age_range_max,age_range,resource_types}",
                rel = "searchCollections"
            )
        }
    }

    fun discoverCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ): HateoasLink {
        return HateoasLink(
            href = getCollectionsRoot()
                .queryParam("discoverable", true)
                .toUriString() + "{&query,subject,projection,page,size,age_range_min,age_range_max,age_range,resource_types}",
            rel = "discoverCollections"
        )
    }

    fun promotedCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ): HateoasLink {
        return HateoasLink(
            href = getCollectionsRoot()
                .queryParam("projection", projection)
                .queryParam("promoted", true)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString(),
            rel = "promotedCollections"
        )
    }

    fun myCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ): HateoasLink? {
        return getIfHasRole(UserRoles.VIEW_COLLECTIONS) { currentUser ->
            HateoasLink(
                href = userCollectionsLink(user = currentUser, projection = projection, page = page, size = size)
                    .toUriString() + "{&query,subject,bookmarked,age_range,resource_types}",
                rel = "myCollections"
            )
        }
    }

    fun collectionsByOwner(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.COLLECTIONS_PAGE_SIZE
    ): HateoasLink? {
        return getIfHasRole(UserRoles.VIEW_ANY_COLLECTION) {
            HateoasLink(
                href = userCollectionsLink(user = "{userId}", projection = projection, page = page, size = size)
                    .toUriString() + "{&query,subject,bookmarked,age_range,resource_types}",
                rel = "collectionsByOwner"
            )
        }
    }

    fun self(id: String? = null): HateoasLink {
        if (id != null) {
            return collectionResourceLink(id, "self")
        }
        return HateoasLink.of(Link(uriComponentsBuilderFactory.getInstance().toUriString()))
    }

    fun next(pageInfo: PageInfo): HateoasLink? {
        return if (pageInfo.hasMoreElements) {
            val currentPage =
                uriComponentsBuilderFactory.getInstance().build().queryParams["page"]?.first()?.toInt() ?: 0

            HateoasLink(
                href = uriComponentsBuilderFactory.getInstance().replaceQueryParam(
                    "page",
                    currentPage + 1
                ).toUriString(),
                rel = "next"
            )
        } else {
            null
        }
    }

    fun bookmark(collection: Collection, user: User): HateoasLink? =
        if (getCurrentUserIfNotAnonymous() == null || collection.isBookmarkedBy(user) || collection.isOwner(user)) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collection.id.value)
                .queryParam("bookmarked", "true")
                .toUriString()
            HateoasLink(href, "bookmark")
        }

    fun unbookmark(collection: Collection, user: User): HateoasLink? {
        return if (!collection.isBookmarkedBy(user) || collection.isOwner(user)) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collection.id.value)
                .queryParam("bookmarked", "false")
                .toUriString()
            HateoasLink(href, "unbookmark")
        }
    }

    fun interactedWith(collection: Collection): HateoasLink {
        return HateoasLink.of(
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(EventController::class.java)
                    .logCollectionInteractedWithEvent(collectionId = collection.id.value, data = null)
            ).withRel(Rels.LOG_COLLECTION_INTERACTION)
        )
    }

    fun projections() =
        ProjectionsCollectionsLinkBuilder(
            uriComponentsBuilderFactory
        )

    private fun collectionResourceLink(id: String?, rel: String): HateoasLink {
        return if (id == null) {
            HateoasLink.of(
                Link(
                    getCollectionsRoot().toUriString() + "/{id}{?referer,shareCode}",
                    rel
                )
            )
        } else {
            HateoasLink.of(
                Link(
                    getCollectionsRoot()
                        .pathSegment(id)
                        .toUriString(), rel
                )
            )
        }
    }

    private fun userCollectionsLink(
        projection: Projection,
        page: Int,
        size: Int,
        user: String
    ) = getUserCollectionsRoot(user)
        .queryParam("projection", projection)
        .queryParam("page", page)
        .queryParam("size", size)

    private fun getCollectionsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/collections")
        .replaceQueryParams(null)

    private fun getUserCollectionsRoot(userId: String) = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/users/$userId/collections")
        .replaceQueryParams(null)

    class ProjectionsCollectionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
        fun list(): HateoasLink {
            return HateoasLink(
                href = uriComponentsBuilderFactory.getInstance().replaceQueryParam("projection", "list").toUriString(),
                rel = "list"
            )
        }

        fun details(): HateoasLink {
            return HateoasLink(
                href = uriComponentsBuilderFactory.getInstance().replaceQueryParam(
                    "projection",
                    "details"
                ).toUriString(),
                rel = "details"
            )
        }
    }
}
