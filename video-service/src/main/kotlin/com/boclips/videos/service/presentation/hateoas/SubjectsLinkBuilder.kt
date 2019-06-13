package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.presentation.subject.SubjectResource
import org.springframework.hateoas.Link
import org.springframework.stereotype.Component

@Component
class SubjectsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun subjects(rel: String = "subjects"): Link {
        return Link(getSubjectRoot().toUriString(), rel)
    }

    fun subject(subject: SubjectResource, rel: String = "self"): Link {
        return Link(getSubjectRoot().pathSegment(subject.id).toUriString(), rel)
    }

    private fun getSubjectRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/subjects")
        .replaceQueryParams(null)
}