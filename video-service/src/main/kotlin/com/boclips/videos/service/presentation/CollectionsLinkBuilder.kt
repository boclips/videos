package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.PageInfo
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

    fun collection(
        id: String?
    ): Link {
        return if (id == null) {
            Link(
                getCollectionsRoot().toUriString() + "/{id}",
                "userCollection"
            )
        } else {
            val href = getCollectionsRoot()
                .pathSegment(id)
                .toUriString()
            Link(href, "userCollection")
        }
    }

    fun publicCollections(
        projection: Projections = Projections.list,
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

    fun collectionsByUser(
        owner: String,
        projection: Projections = Projections.list,
        page: Int = 0,
        size: Int = CollectionsController.PUBLIC_COLLECTIONS_PAGE_SIZE
    ): Link {
        val href = getCollectionsRoot()
            .queryParam("projection", projection)
            .queryParam("owner", owner)
            .queryParam("page", page)
            .queryParam("size", size)
            .toUriString()
        return Link(href, "userCollections")
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

    fun projections() = ProjectionsCollectionsLinkBuilder(uriComponentsBuilderFactory)

    private fun getCollectionsRoot() = uriComponentsBuilderFactory.getInstance().replacePath("/v1/collections")

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