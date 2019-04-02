package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles.VIEW_DISABLED_VIDEOS
import getCurrentUserIfNotAnonymous
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController(
    private val collectionsLinkBuilder: CollectionsLinkBuilder
) {
    @GetMapping
    fun search(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        return Resource(
            "", listOfNotNull(
                VideoController.videoLink(),
                EventController.createPlaybackEventLink(),
                EventController.createNoResultsEventLink(),

                addIfAuthenticated { collectionsLinkBuilder.publicCollections() },
                addIfAuthenticated { VideoController.videosLink() },
                addIfAuthenticated { VideoController.searchLink() },
                addIfAuthenticated { collectionsLinkBuilder.collection(null) },
                addIfAuthenticated { userId -> collectionsLinkBuilder.collectionsByUser(userId) },

                if (request.isUserInRole(VIEW_DISABLED_VIDEOS)) VideoController.adminSearchLink() else null
            )
        )
    }
}

private fun addIfAuthenticated(linkSupplier: (user: String) -> Link): Link? =
    getCurrentUserIfNotAnonymous()?.let { linkSupplier(it.id) }
