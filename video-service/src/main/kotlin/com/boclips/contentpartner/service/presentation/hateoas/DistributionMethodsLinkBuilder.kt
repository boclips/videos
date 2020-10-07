package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.hateoas.UriComponentsBuilderFactory
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class DistributionMethodsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    fun distributionMethods(rel: String = "distributionMethods") =
        getIfHasAnyRole(UserRoles.VIEW_DISTRIBUTION_METHODS) {
            Link.of(getRoot().toUriString(), rel)
        }

    private fun getRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/distribution-methods")
        .replaceQueryParams(null)
}
