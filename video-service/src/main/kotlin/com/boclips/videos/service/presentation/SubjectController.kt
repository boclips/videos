package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/subjects")
class SubjectController(
    private val getSubjects: GetSubjects,
    private val createSubject: CreateSubject,
    private val subjectsLinkBuilder: SubjectsLinkBuilder
) {
    @GetMapping
    fun subjects(): Resources<Resource<*>> {
        return Resources(getSubjects().map { Resource(it) }, subjectsLinkBuilder.self())
    }

    @PostMapping
    fun createASubject(@Valid @RequestBody createSubjectRequest: CreateSubjectRequest): ResponseEntity<*> {
        createSubject(createSubjectRequest)

        return ResponseEntity<Any>(HttpStatus.CREATED)
    }
}