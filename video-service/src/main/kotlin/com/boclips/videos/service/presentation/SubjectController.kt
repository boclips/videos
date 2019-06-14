package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.GetSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.subject.SubjectResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/subjects")
class SubjectController(
    private val getSubject: GetSubject,
    private val getSubjects: GetSubjects,
    private val createSubject: CreateSubject,
    private val subjectsLinkBuilder: SubjectsLinkBuilder
) {

    @GetMapping("/{id}")
    fun subject(@PathVariable id: String): Resource<SubjectResource> =
        getSubject(id).let { Resource(it, subjectsLinkBuilder.subject(it, "self")) }

    @GetMapping
    fun subjects(): Resources<Resource<*>> {
        return Resources(getSubjects().map { Resource(it, subjectsLinkBuilder.subject(it, "self")) }, subjectsLinkBuilder.subjects("self"))
    }

    @PostMapping
    fun createASubject(@Valid @RequestBody createSubjectRequest: CreateSubjectRequest): ResponseEntity<Any> {
        val subject = createSubject(createSubjectRequest)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, subjectsLinkBuilder.subject(subject).href)
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }
}
