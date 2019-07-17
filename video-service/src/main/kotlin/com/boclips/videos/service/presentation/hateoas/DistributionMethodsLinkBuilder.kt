package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class DistributionMethodsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun distributionMethods(rel: String = "distributionMethods") =
        getIfHasAnyRole(UserRoles.VIEW_DISTRIBUTION_METHODS){
            Link(getRoot().toUriString(), rel)
        }

    private fun getRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/distribution-methods")
        .replaceQueryParams(null)
}
