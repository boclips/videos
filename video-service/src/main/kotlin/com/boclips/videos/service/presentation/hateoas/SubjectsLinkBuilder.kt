package com.boclips.videos.service.presentation.hateoas

import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class SubjectsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun subjects(): Link {
        return Link(getSubjectRoot().toUriString(), "subjects")
    }

    fun self(): Link {
        return Link(getSubjectRoot().toUriString())
    }

    private fun getSubjectRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/subjects")
        .replaceQueryParams(null)
}