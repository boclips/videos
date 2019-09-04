package com.boclips.videos.service.presentation

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DistributionMethodsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder
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
    private val eventsLinkBuilder: EventsLinkBuilder,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val disciplinesLinkBuilder: DisciplinesLinkBuilder,
    private val distributionMethodsLinkBuilder: DistributionMethodsLinkBuilder,
    private val tagsLinkBuilder: TagsLinkBuilder
) {
    @GetMapping
    fun get(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        return Resource(
            "", listOfNotNull(
                videosLinkBuilder.videoLink(),
                getIfHasRole(UserRoles.TEACHER) { eventsLinkBuilder.createPlaybackEventLink() },
                getIfHasRole(UserRoles.TEACHER) { eventsLinkBuilder.createNoResultsEventLink() },
                collectionsLinkBuilder.publicCollections(),
                subjectsLinkBuilder.subjects(),
                distributionMethodsLinkBuilder.distributionMethods(),
                videosLinkBuilder.adminSearchLink(),

                videosLinkBuilder.videosLink(),
                videosLinkBuilder.searchVideosLink(),
                collectionsLinkBuilder.bookmarkedCollections(),
                collectionsLinkBuilder.searchCollections(),
                collectionsLinkBuilder.collection(null),
                collectionsLinkBuilder.collectionsByOwner(),
                collectionsLinkBuilder.myCollections(),
                collectionsLinkBuilder.createCollection(),
                contentPartnersLinkBuilder.contentPartnerLink(null),
                contentPartnersLinkBuilder.contentPartnersLink(),
                disciplinesLinkBuilder.disciplines(),
                tagsLinkBuilder.tags()
            )
        )
    }
}
