package com.boclips.contentpartner.service.presentation.ageRange

import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class AgeRangeLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val AGE_RANGES = "ageRanges"
    }

    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(AgeRangeController::class.java).getAgeRange(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun ageRanges(): Link? {
        return getIfHasRole(UserRoles.VIEW_AGE_RANGES) {
            Link.of(
                getAgeRangesRoot().build().toUriString(),
                Rels.AGE_RANGES
            )
        }
    }

    private fun getAgeRangesRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/age-ranges")
        .replaceQueryParams(null)
}
