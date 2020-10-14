package com.boclips.videos.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class NewSuggestionsLinkBuilder(val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val NEW_SUGGESTIONS = "new-suggestions"
    }

    fun suggestions(): HateoasLink? {
        return when {
            UserExtractor.currentUserHasRole(UserRoles.VIEW_VIDEOS) -> {
                HateoasLink.of(
                    Link.of(
                        getRoot()
                            .queryParam("query", "{query}")
                            .build()
                            .toUriString(),
                        Rels.NEW_SUGGESTIONS
                    )
                )
            }
            else -> null
        }
    }

    private fun getRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/new-suggestions")
        .replaceQueryParams(null)
}