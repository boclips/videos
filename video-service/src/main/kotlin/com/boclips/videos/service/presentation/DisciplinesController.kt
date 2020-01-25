package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.discipline.CreateDisciplineRequest
import com.boclips.videos.api.response.discipline.DisciplineResource
import com.boclips.videos.api.response.discipline.DisciplinesResource
import com.boclips.videos.service.application.disciplines.CreateDiscipline
import com.boclips.videos.service.application.disciplines.GetDiscipline
import com.boclips.videos.service.application.disciplines.GetDisciplines
import com.boclips.videos.service.application.disciplines.ReplaceDisciplineSubjects
import com.boclips.videos.service.domain.service.AccessRuleService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.presentation.hateoas.DisciplinesLinkBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/disciplines")
class DisciplinesController(
    private val getDiscipline: GetDiscipline,
    private val getDisciplines: GetDisciplines,
    private val createDiscipline: CreateDiscipline,
    private val replaceDisciplineSubjects: ReplaceDisciplineSubjects,
    private val disciplinesLinkBuilder: DisciplinesLinkBuilder,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {

    @GetMapping("/{id}")
    fun discipline(@PathVariable id: String): DisciplineResource {
        return getDiscipline(id)
    }

    @GetMapping
    fun disciplines(): DisciplinesResource {
        return getDisciplines()
    }

    @PostMapping
    fun createADiscipline(@Valid @RequestBody createDisciplineRequest: CreateDisciplineRequest): ResponseEntity<Any> {
        val discipline = createDiscipline(createDisciplineRequest)

        val headers = HttpHeaders().apply {
            set(HttpHeaders.LOCATION, disciplinesLinkBuilder.discipline(id = discipline.id.value)?.href)
        }

        return ResponseEntity(discipline(discipline.id.value), headers, HttpStatus.CREATED)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(path = ["/{id}/subjects"])
    fun putSubjects(@PathVariable id: String, @RequestBody body: String) {
        val subjectIds = body.lines().map { it.substringAfter("/subjects/") }
        replaceDisciplineSubjects(disciplineId = id, subjectIds = subjectIds)
    }
}
