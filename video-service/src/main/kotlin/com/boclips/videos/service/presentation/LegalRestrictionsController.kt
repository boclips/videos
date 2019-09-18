package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.legal.restrictions.CreateLegalRestrictions
import com.boclips.videos.service.application.legal.restrictions.FindAllLegalRestrictions
import com.boclips.videos.service.application.legal.restrictions.FindLegalRestrictions
import com.boclips.videos.service.application.legal.restrictions.LegalRestrictionsResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/legal-restrictions")
class LegalRestrictionsController(
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val findLegalRestrictions: FindLegalRestrictions,
    private val findAllLegalRestrictions: FindAllLegalRestrictions
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun post(@RequestParam text: String?): Resource<LegalRestrictionsResource> {
        return createLegalRestrictions(text = text).hateoas()
    }

    @GetMapping
    fun get(): Resources<LegalRestrictionsResource> {
        return Resources(findAllLegalRestrictions())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: String): ResponseEntity<Resource<LegalRestrictionsResource>> {
        val resource = findLegalRestrictions(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(resource.hateoas(), HttpStatus.OK)
    }

    companion object {

        fun LegalRestrictionsResource.hateoas(): Resource<LegalRestrictionsResource> {
            return Resource(this, restrictionsLink(this.id))
        }

        private fun restrictionsLink(id: String): Link {
            return linkTo(methodOn(LegalRestrictionsController::class.java).getById(id)).withSelfRel()
        }
    }
}