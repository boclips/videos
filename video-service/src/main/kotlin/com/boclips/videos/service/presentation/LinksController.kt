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
class LinksController {
    @GetMapping
    fun search(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        val currentUserId = getCurrentUserIfNotAnonymous()?.id

        return Resource(
            "", listOfNotNull(
                VideoController.videoLink(),
                EventController.createPlaybackEventLink(),
                EventController.createNoResultsEventLink(),
                CollectionsController.getPublicCollections().withRel("publicCollections"),

                addIfAuthenticated(VideoController.videosLink()),
                addIfAuthenticated(VideoController.searchLink()),
                addIfAuthenticated(CollectionsController.getUserCollectionLink(null).withRel("userCollection")),
                addIfAuthenticated(CollectionsController.postUserCollectionsLink().withRel("userCollections")),
                currentUserId?.let { addIfAuthenticated(CollectionsController.getUserCollectionsDetailsLink(it).withRel("userCollectionsDetails")) },
                currentUserId?.let { addIfAuthenticated(CollectionsController.getUserCollectionsListLink(it).withRel("userCollectionsList")) },

                if (request.isUserInRole(VIEW_DISABLED_VIDEOS)) VideoController.adminSearchLink() else null
            )
        )
    }
}

private fun addIfAuthenticated(link: Link) = getCurrentUserIfNotAnonymous()?.let { link }
