package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContentCategoriesLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractLegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.ContractsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.DistributionMethodsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.LegacyContentPartnerLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.LegalRestrictionsLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.MarketingStatusLinkBuilder
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.presentation.hateoas.*
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
    private val videosLinkBuilder: VideosLinkBuilder,
    private val collectionsLinkBuilder: CollectionsLinkBuilder,
    private val subjectsLinkBuilder: SubjectsLinkBuilder,
    private val ageRangesLinkBuilder: AgeRangeLinkBuilder,
    private val disciplinesLinkBuilder: DisciplinesLinkBuilder,
    private val tagsLinkBuilder: TagsLinkBuilder,
    private val contentWarningLinkBuilder: ContentWarningLinkBuilder,
    private val suggestionLinkBuilder: SuggestionLinkBuilder,
    private val videoTypeLinkBuilder: VideoTypeLinkBuilder,
    private val legacyContentPartnerLinkBuilder: LegacyContentPartnerLinkBuilder,
    private val channelLinkBuilder: ChannelLinkBuilder,
    private val contractsLinkBuilder: ContractsLinkBuilder,
    private val contentCategoriesLinkBuilder: ContentCategoriesLinkBuilder,
    private val contractLegalRestrictionsLinkBuilder: ContractLegalRestrictionsLinkBuilder,
    private val legalRestrictionsLinkBuilder: LegalRestrictionsLinkBuilder,
    private val marketingStatusLinkBuilder: MarketingStatusLinkBuilder,
    private val distributionMethodsLinkBuilder: DistributionMethodsLinkBuilder,
    private val eventsLinkBuilder: EventsLinkBuilder,
    private val attachmentTypeLinkBuilder: AttachmentTypeLinkBuilder,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {
    @GetMapping
    fun get(request: SecurityContextHolderAwareRequestWrapper): LinksResource {
        return LinksResource(
            links = listOfNotNull(
                subjectsLinkBuilder.subjects(),

                videosLinkBuilder.videoLink(),
                videosLinkBuilder.getCaptions(),
                videosLinkBuilder.searchVideosLink(),

                videoTypeLinkBuilder.videoTypes(),

                attachmentTypeLinkBuilder.attachmentTypes(),

                collectionsLinkBuilder.discoverCollections(),
                collectionsLinkBuilder.promotedCollections(),
                collectionsLinkBuilder.searchCollections(),
                collectionsLinkBuilder.searchAllCollections(),
                collectionsLinkBuilder.collection(null),
                collectionsLinkBuilder.myOwnCollections(),
                collectionsLinkBuilder.mySavedCollections(),
                collectionsLinkBuilder.createCollection(),

                eventsLinkBuilder.createPlaybackEventsLink(),
                eventsLinkBuilder.createSearchQueryCompletionsSuggestedEventLink(),

                legalRestrictionsLinkBuilder.getAllLink(),
                contentWarningLinkBuilder.getAllLink(),
                disciplinesLinkBuilder.disciplines(),
                tagsLinkBuilder.tags(),
                ageRangesLinkBuilder.ageRanges(),
                suggestionLinkBuilder.suggestions(),

                // belong to contentpartner-service links
                distributionMethodsLinkBuilder.distributionMethods(),
                legacyContentPartnerLinkBuilder.contentPartnerLink(null),
                legacyContentPartnerLinkBuilder.contentPartnersLink(),
                channelLinkBuilder.channelLink(null),
                channelLinkBuilder.channelsLink(),
                channelLinkBuilder.channelsSignedUploadLink(),
                contractsLinkBuilder.contractLink(null),
                contractsLinkBuilder.contractsLink(),
                contractsLinkBuilder.createContractLink(),
                contractsLinkBuilder.createSignedUploadLink(),
                contractLegalRestrictionsLinkBuilder.contractLegalRestrictions(),
                marketingStatusLinkBuilder.marketingStatuses(),
                contentCategoriesLinkBuilder.contentCategories()
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
