package com.boclips.videos.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class SuggestionLinkBuilder(val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val SUGGESTIONS = "suggestions"
    }

    fun suggestions(): HateoasLink? {
        return when {
            currentUserHasRole(UserRoles.VIEW_VIDEOS) -> {
                HateoasLink.of(
                    Link.of(
                        getRoot()
                            .queryParam("query", "{query}")
                            .build()
                            .toUriString(),
                        Rels.SUGGESTIONS
                    )
                )
            }
            else -> null
        }
    }

    private fun getRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/suggestions")
        .replaceQueryParams(null)
}
