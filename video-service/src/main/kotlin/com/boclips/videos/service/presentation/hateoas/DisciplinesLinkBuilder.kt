package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class DisciplinesLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    fun disciplines(rel: String = "disciplines") = getIfHasRole(UserRoles.VIEW_DISCIPLINES) {
        Link(getDisciplineRoot().toUriString(), rel)
    }

    fun discipline(discipline: DisciplineResource, rel: String = "self") = getIfHasRole(UserRoles.VIEW_DISCIPLINES) {
        Link(getDisciplineRoot().pathSegment(discipline.id).toUriString(), rel)
    }

    fun subjectsForDiscipline(discipline: DisciplineResource) = getIfHasRole(UserRoles.UPDATE_DISCIPLINES) {
        Link(getDisciplineRoot().pathSegment(discipline.id).pathSegment("subjects").toUriString(), "subjects")
    }

    private fun getDisciplineRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/disciplines")
        .replaceQueryParams(null)
}

