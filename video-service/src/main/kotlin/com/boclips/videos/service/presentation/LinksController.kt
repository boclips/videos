package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles.VIEW_DISABLED_VIDEOS
import org.springframework.hateoas.Resource
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {

    @GetMapping
    fun search(request: SecurityContextHolderAwareRequestWrapper): Resource<String> = Resource(
        "", listOfNotNull(
            VideoController.searchLink(),
            VideoController.videoLink(),
            VideoController.videosLink(),
            if (request.isUserInRole(VIEW_DISABLED_VIDEOS)) VideoController.adminSearchLink() else null,
            EventController.createPlaybackEventLink(),
            EventController.createNoResultsEventLink(),
            CollectionsController.getUserCollectionLink(null).withRel("userCollection"),
            CollectionsController.getUserCollectionsDetailsLink().withRel("userCollections"),
            CollectionsController.getUserCollectionsDetailsLink().withRel("userCollectionsDetails"),
            CollectionsController.getUserCollectionsListLink().withRel("userCollectionsList")
        )
    )
}
