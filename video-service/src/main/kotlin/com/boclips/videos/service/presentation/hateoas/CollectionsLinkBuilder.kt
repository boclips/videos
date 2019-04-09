package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.domain.model.PageInfo
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.hateoas.Link
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest

@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class UriComponentsBuilderFactory(val request: HttpServletRequest) {
    fun getInstance() = UriComponentsBuilder.fromHttpRequest(ServletServerHttpRequest(request))
}

@Component
class CollectionsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun collection(id: String?) = collectionResourceLink(id, "collection")

    fun editCollection(collectionResource: CollectionResource) =
        if (collectionResource.isMine) collectionResourceLink(collectionResource.id, "edit") else null

    fun removeCollection(collectionResource: CollectionResource) =
        if (collectionResource.isMine) collectionResourceLink(collectionResource.id, "remove") else null

    fun addVideoToCollection(collectionResource: CollectionResource) =
        if (collectionResource.isMine) Link(
            getCollectionsRoot()
                .pathSegment(collectionResource.id)
                .pathSegment("videos")
                .toUriString() + "/{video_id}", "addVideo"
        ) else null

    fun removeVideoFromCollection(collectionResource: CollectionResource) =
        if (collectionResource.isMine) Link(
            getCollectionsRoot()
                .pathSegment(collectionResource.id)
                .pathSegment("videos")
                .toUriString() + "/{video_id}", "removeVideo"
        ) else null

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

    fun collections() = Link(getCollectionsRoot().toUriString(), "collections")

    fun publicCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.PUBLIC_COLLECTIONS_PAGE_SIZE
    ): Link {
        val href = getCollectionsRoot()
            .queryParam("projection", projection)
            .queryParam("public", true)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString()
        return Link(href, "publicCollections")
    }

    fun bookmarkedCollections(
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.PUBLIC_COLLECTIONS_PAGE_SIZE
    ): Link {
        val href = getCollectionsRoot()
            .queryParam("projection", projection)
            .queryParam("public", true)
            .queryParam("bookmarked", true)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString()
        return Link(href, "bookmarkedCollections")
    }

    fun collectionsByUser(
        owner: String,
        projection: Projection = Projection.list,
        page: Int = 0,
        size: Int = CollectionsController.PUBLIC_COLLECTIONS_PAGE_SIZE
    ): Link {
        val href = getCollectionsRoot()
            .queryParam("projection", projection)
            .queryParam("owner", owner)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString()
        return Link(href, "myCollections")
    }

    fun self(): Link {
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
        if (collectionResource.isBookmarked || collectionResource.isMine || !collectionResource.isPublic) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collectionResource.id)
                .queryParam("bookmarked", "true")
                .toUriString()
            Link(href, "bookmark")
        }

    fun unbookmark(collectionResource: CollectionResource) =
        if (!collectionResource.isBookmarked || collectionResource.isMine || !collectionResource.isPublic) {
            null
        } else {
            val href = getCollectionsRoot().pathSegment(collectionResource.id)
                .queryParam("bookmarked", "false")
                .toUriString()
            Link(href, "unbookmark")
        }

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
