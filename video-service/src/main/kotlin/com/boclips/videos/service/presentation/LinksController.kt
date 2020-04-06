package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContentCategoriesLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnerContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.DistributionMethodsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.LegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.NewLegalRestrictionsLinkBuilder
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.hateoas.CollectionsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import com.boclips.videos.service.presentation.hateoas.EventsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideoTypeLinkBuilder
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.hateoas.Link
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
    private val contentPartnerContractsLinkBuilder: ContentPartnerContractsLinkBuilder,
    private val disciplinesLinkBuilder: DisciplinesLinkBuilder,
    private val distributionMethodsLinkBuilder: DistributionMethodsLinkBuilder,
    private val tagsLinkBuilder: TagsLinkBuilder,
    private val videoTypeLinkBuilder: VideoTypeLinkBuilder,
    private val eventsLinkBuilder: EventsLinkBuilder,
    private val contentCategoriesLinkBuilder: ContentCategoriesLinkBuilder,
    private val legalRestrictionsLinkBuilder: LegalRestrictionsLinkBuilder,
    private val ageRangesLinkBuilder: AgeRangeLinkBuilder,
    private val marketingStatusLinkBuilder: MarketingStatusLinkBuilder,
    private val newLegalRestrictionsLinkBuilder: NewLegalRestrictionsLinkBuilder,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    @GetMapping
    fun get(request: SecurityContextHolderAwareRequestWrapper): LinksResource {
        return LinksResource(
            links = listOfNotNull(
                distributionMethodsLinkBuilder.distributionMethods(), //belongs to contentpartner-service links
                videosLinkBuilder.videoLink(),
                collectionsLinkBuilder.publicCollections(),
                subjectsLinkBuilder.subjects(),
                videosLinkBuilder.adminSearchLink(),
                videosLinkBuilder.adminVideoSearchLink(),
                collectionsLinkBuilder.adminCollectionSearch(),
                videosLinkBuilder.searchVideosLink(),
                collectionsLinkBuilder.bookmarkedCollections(),
                collectionsLinkBuilder.promotedCollections(),
                collectionsLinkBuilder.searchPublicCollections(),
                collectionsLinkBuilder.searchCollections(),
                collectionsLinkBuilder.collection(null),
                collectionsLinkBuilder.collectionsByOwner(),
                collectionsLinkBuilder.myCollections(),
                collectionsLinkBuilder.mySavedCollections(),
                collectionsLinkBuilder.createCollection(),
                legalRestrictionsLinkBuilder.getAllLink(),
                contentPartnersLinkBuilder.contentPartnerLink(null),
                contentPartnersLinkBuilder.contentPartnersLink(),
                contentPartnersLinkBuilder.contentPartnersSignedUploadLink(),
                contentPartnerContractsLinkBuilder.contentPartnerContractLink(null),
                contentPartnerContractsLinkBuilder.contentPartnerContractsLink(),
                contentPartnerContractsLinkBuilder.createContractLink(),
                newLegalRestrictionsLinkBuilder.newLegalRestrictionLink(null),
                newLegalRestrictionsLinkBuilder.newLegalRestrictionsLink(),
                disciplinesLinkBuilder.disciplines(),
                tagsLinkBuilder.tags(),
                videoTypeLinkBuilder.videoTypes(),
                eventsLinkBuilder.createPlaybackEventsLink(),
                contentCategoriesLinkBuilder.contentCategories(),
                ageRangesLinkBuilder.ageRanges(),
                marketingStatusLinkBuilder.marketingStatuses()
            )
        )
    }
}

class LinksResource(@JsonIgnore private val links: List<Any>) {
    @JsonProperty("_links")
    fun getLinks(): Map<String, HateoasLink> {
        return links
            .map {
                return@map when (it) {
                    is Link -> HateoasLink.of(it)
                    is HateoasLink -> it
                    else -> throw IllegalStateException("Link type unrecognised.")
                }
            }
            .map {
                it.rel to it
            }.toMap()
    }
}
