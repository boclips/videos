package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles.VIEW_DISABLED_VIDEOS
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
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
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val videosLinkBuilder: VideosLinkBuilder,
    private val subjectsLinkBuilder: SubjectsLinkBuilder
) {
    @GetMapping
    fun search(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        return Resource(
            "", listOfNotNull(
                videosLinkBuilder.videoLink(),
                EventController.createPlaybackEventLink(),
                EventController.createNoResultsEventLink(),
                collectionsLinkBuilder.publicCollections(),
                subjectsLinkBuilder.subjects(),

                addIfAuthenticated { collectionsLinkBuilder.bookmarkedCollections() },
                addIfAuthenticated { videosLinkBuilder.videosLink() },
                addIfAuthenticated { videosLinkBuilder.searchLink() },
                addIfAuthenticated { collectionsLinkBuilder.collection(null) },
                addIfAuthenticated { userId -> collectionsLinkBuilder.collectionsByUser(userId) },
                addIfAuthenticated { collectionsLinkBuilder.collections() },

                if (request.isUserInRole(VIEW_DISABLED_VIDEOS)) videosLinkBuilder.adminSearchLink() else null
            )
        )
    }
}

private fun addIfAuthenticated(linkSupplier: (user: String) -> Link): Link? =
    getCurrentUserIfNotAnonymous()?.let { linkSupplier(it.id) }
