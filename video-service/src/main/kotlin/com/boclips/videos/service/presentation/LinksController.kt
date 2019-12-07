package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.presentation.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.LegalRestrictionsController
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DistributionMethodsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideoTypeLinkBuilder
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
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val disciplinesLinkBuilder: DisciplinesLinkBuilder,
    private val distributionMethodsLinkBuilder: DistributionMethodsLinkBuilder,
    private val tagsLinkBuilder: TagsLinkBuilder,
    private val videoTypeLinkBuilder: VideoTypeLinkBuilder
) : BaseController() {
    @GetMapping
    fun get(request: SecurityContextHolderAwareRequestWrapper): Resource<String> {
        return Resource(
            "", listOfNotNull(
                videosLinkBuilder.videoLink(),
                collectionsLinkBuilder.publicCollections(),
                subjectsLinkBuilder.subjects(),
                distributionMethodsLinkBuilder.distributionMethods(),
                videosLinkBuilder.adminSearchLink(),
                videosLinkBuilder.adminVideoSearchLink(),
                collectionsLinkBuilder.adminCollectionSearch(),

                videosLinkBuilder.videosLink(),
                videosLinkBuilder.searchVideosLink(),
                collectionsLinkBuilder.bookmarkedCollections(),
                collectionsLinkBuilder.searchPublicCollections(),
                collectionsLinkBuilder.searchCollections(),
                collectionsLinkBuilder.collection(null),
                collectionsLinkBuilder.collectionsByOwner(),
                collectionsLinkBuilder.myCollections(),
                collectionsLinkBuilder.createCollection(),
                LegalRestrictionsController.getAllLink(),
                contentPartnersLinkBuilder.contentPartnerLink(null),
                contentPartnersLinkBuilder.contentPartnersLink(),
                disciplinesLinkBuilder.disciplines(),
                tagsLinkBuilder.tags(),
                videoTypeLinkBuilder.videoTypes()
            )
        )
    }
}
