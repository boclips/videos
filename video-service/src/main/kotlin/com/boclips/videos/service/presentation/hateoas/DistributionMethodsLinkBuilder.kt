package com.boclips.videos.service.presentation.hateoas

import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class DistributionMethodsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun distributionMethods(rel: String = "distributionMethods"): Link {
        return Link(getRoot().toUriString(), rel)
    }

    private fun getRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/distribution-methods")
        .replaceQueryParams(null)
}