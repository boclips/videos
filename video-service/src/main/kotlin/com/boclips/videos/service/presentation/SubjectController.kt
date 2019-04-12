package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.subject.SubjectResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/subjects")
class SubjectController(
    private val getSubjects: GetSubjects,
    private val subjectsLinkBuilder: SubjectsLinkBuilder
) {
    @GetMapping
    fun subjects(): Resources<Resource<*>> {
        return Resources(getSubjects().map { Resource(it) }, subjectsLinkBuilder.self())
    }
}