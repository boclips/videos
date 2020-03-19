package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class DisciplinesLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    fun disciplines(rel: String = "disciplines") = getIfHasRole(UserRoles.VIEW_DISCIPLINES) {
        Link(getDisciplineRoot().toUriString(), rel)
    }

    fun discipline(rel: String = "self", id: String) = getIfHasRole(UserRoles.VIEW_DISCIPLINES) {
        Link(getDisciplineRoot().pathSegment(id).toUriString(), rel)
    }

    fun subjectsForDiscipline(id: String) = getIfHasRole(UserRoles.UPDATE_DISCIPLINES) {
        Link(getDisciplineRoot().pathSegment(id).pathSegment("subjects").toUriString(), "subjects")
    }

    fun updateDiscipline(id: String) = getIfHasRole(UserRoles.UPDATE_DISCIPLINES) {
        Link(getDisciplineRoot().pathSegment(id).toUriString(), "update")
    }

    private fun getDisciplineRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/disciplines")
        .replaceQueryParams(null)
}

