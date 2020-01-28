package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class SubjectsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun subjects(rel: String = "subjects"): HateoasLink {
        return HateoasLink.of(Link(getSubjectsRoot().toUriString(), rel))
    }

    fun self(id: String): HateoasLink {
        return HateoasLink.of(Link(getSubjectsRoot().pathSegment(id).toUriString(), "self"))
    }

    fun updateSubject(subject: SubjectResource): HateoasLink? {
        return UserExtractor.getIfHasRole(UserRoles.UPDATE_SUBJECTS) {
            HateoasLink.of(Link(getSubjectsRoot().pathSegment(subject.id).toUriString(), "update"))
        }
    }

    private fun getSubjectsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/subjects")
        .replaceQueryParams(null)
}
