package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.SubjectController
import com.boclips.videos.service.presentation.subject.SubjectResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.stereotype.Component

@Component
class SubjectsLinkBuilder {

    fun subjects(rel: String = "subjects"): Link {
        return linkTo(methodOn(SubjectController::class.java).subjects()).withRel(rel)
    }

    fun self(subject: SubjectResource): Link {
        return linkTo(methodOn(SubjectController::class.java).subject(subject.id)).withSelfRel()
    }

    fun updateSubject(subject: SubjectResource): Link? {
        return UserExtractor.getIfHasRole(UserRoles.UPDATE_SUBJECTS) {
            linkTo(
                methodOn(SubjectController::class.java).updateSubjects(
                    id = subject.id,
                    createSubjectRequest = null
                )
            ).withRel("update")
        }
    }
}
