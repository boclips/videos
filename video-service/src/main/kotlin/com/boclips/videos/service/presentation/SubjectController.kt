package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.application.getCurrentUser
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.subject.DeleteSubject
import com.boclips.videos.service.application.subject.GetSubject
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.application.subject.UpdateSubject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.presentation.hateoas.SubjectsLinkBuilder
import com.boclips.videos.service.presentation.subject.CreateSubjectRequest
import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/subjects")
class SubjectController(
    private val getSubject: GetSubject,
    private val deleteSubject: DeleteSubject,
    private val getSubjects: GetSubjects,
    private val createSubject: CreateSubject,
    private val updateSubject: UpdateSubject,
    private val subjectsLinkBuilder: SubjectsLinkBuilder
) {

    @GetMapping("/{id}")
    fun subject(@PathVariable id: String): Resource<SubjectResource> =
        getSubject(id).let { Resource(it, subjectsLinkBuilder.self(it)) }

    @GetMapping
    fun subjects(): Resources<Resource<*>> {
        val subjectResources = getSubjects().map {
            Resource(
                it, listOfNotNull(
                    subjectsLinkBuilder.self(it),
                    subjectsLinkBuilder.updateSubject(it)
                )
            )
        }
        return Resources(
            subjectResources,
            subjectsLinkBuilder.subjects("self")
        )
    }

    @DeleteMapping("/{id}")
    fun removeSubjects(@PathVariable id: String): ResponseEntity<Void> {
        deleteSubject(SubjectId(value = id), getCurrentUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping("/{id}")
    fun updateSubjects(@PathVariable id: String, @RequestBody createSubjectRequest: CreateSubjectRequest?): ResponseEntity<Void> {
        updateSubject(SubjectId(value = id), createSubjectRequest!!.name)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping
    fun createASubject(@Valid @RequestBody createSubjectRequest: CreateSubjectRequest): ResponseEntity<Any> {
        val subject = try {
            createSubject(createSubjectRequest)
        } catch (e: SubjectExistsException) {
            throw InvalidRequestApiException(
                ExceptionDetails(
                    error = "Error creating subject",
                    message = "The subject ${createSubjectRequest.name} already exists",
                    status = HttpStatus.CONFLICT
                )
            )
        }
        val headers = HttpHeaders().apply {
            val subjectResource = subject.let { SubjectResource(id = it.id.value, name = it.name) }
            set(HttpHeaders.LOCATION, subjectsLinkBuilder.self(subjectResource).href)
        }
        return ResponseEntity(headers, HttpStatus.CREATED)
    }
}
