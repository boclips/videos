package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
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
    private val subjectsLinkBuilder: SubjectsLinkBuilder,
    private val eventsLinkBuilder: EventsLinkBuilder
) {
    @GetMapping
    fun search(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        return Resource(
            "", listOfNotNull(
                videosLinkBuilder.videoLink(),
                eventsLinkBuilder.createPlaybackEventLink(),
                eventsLinkBuilder.createNoResultsEventLink(),
                collectionsLinkBuilder.publicCollections(),
                subjectsLinkBuilder.subjects(),
                videosLinkBuilder.adminSearchLink(),

                collectionsLinkBuilder.bookmarkedCollections(),
                videosLinkBuilder.videosLink(),
                videosLinkBuilder.searchLink(),
                collectionsLinkBuilder.collection(null),
                collectionsLinkBuilder.collectionsByUser(),
                collectionsLinkBuilder.createCollection()

            )
        )
    }
}
