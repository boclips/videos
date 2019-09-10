package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.SubjectController
import com.boclips.videos.service.presentation.subject.SubjectResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class SubjectsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {

    fun subjects(rel: String = "subjects"): Link {
        return Link(getSubjectRoot().toUriString(), rel)
    }

    fun self(subject: SubjectResource): Link {
        return Link(getSubjectRoot().pathSegment(subject.id).toUriString(), "self")
    }

    fun updateSubject(subject: SubjectResource): Link? {
        return UserExtractor.getIfHasRole(UserRoles.UPDATE_SUBJECTS) {
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(SubjectController::class.java).updateSubjects(
                    id = subject.id,
                    createSubjectRequest = null
                )
            ).withRel("update")
        }
    }

    private fun getSubjectRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/subjects")
        .replaceQueryParams(null)
}
