package com.boclips.contentpartner.service.presentation.newlegalrestriction

import com.boclips.contentpartner.service.application.exceptions.NewLegalRestrictionsNotFountException
import com.boclips.contentpartner.service.application.newlegalrestriction.CreateNewLegalRestriction
import com.boclips.contentpartner.service.application.newlegalrestriction.FindAllNewLegalRestrictions
import com.boclips.contentpartner.service.application.newlegalrestriction.FindOneNewLegalRestriction
import com.boclips.contentpartner.service.domain.model.newlegalrestriction.SingleLegalRestriction
import com.boclips.contentpartner.service.presentation.BaseController
import com.boclips.contentpartner.service.presentation.converters.NewLegalRestrictionsToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.NewLegalRestrictionsLinkBuilder
import com.boclips.videos.api.request.newlegalrestrictions.NewLegalRestrictionResource
import com.boclips.videos.api.response.newlegalrestriction.NewLegalRestrictionsResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/new-legal-restrictions")
class NewLegalRestrictionsController(
    private val fetchAll: FindAllNewLegalRestrictions,
    private val fetchOne: FindOneNewLegalRestriction,
    private val create: CreateNewLegalRestriction,
    private val toResourceConverter: NewLegalRestrictionsToResourceConverter,
    private val linksBuilder: NewLegalRestrictionsLinkBuilder
) : BaseController() {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(): ResponseEntity<NewLegalRestrictionsResource> {
        val allLegalRestrictions = fetchAll()

        val convertedRestrictions = toResourceConverter(allLegalRestrictions)

        return ResponseEntity(convertedRestrictions, HttpStatus.OK)
    }

    @GetMapping("/type/{legalRestrictionType}")
    @ResponseStatus(HttpStatus.OK)
    fun getOne(
        @PathVariable("legalRestrictionType") @NotBlank legalRestrictionType: String?
    ): ResponseEntity<NewLegalRestrictionResource> {
        val resource = fetchOne(legalRestrictionType!!)?.let(toResourceConverter::toSingleResource)
            ?: throw NewLegalRestrictionsNotFountException("No legal restriction found with id=$legalRestrictionType")

        return ResponseEntity(resource, HttpStatus.OK)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: NewLegalRestrictionResource
    ): ResponseEntity<Void> {
        try {
            val restrictionId = request.id
            val restrictions = request.restrictions

            val newLegalRestriction = create(
                id = restrictionId,
                restriction = restrictions.map { SingleLegalRestriction(id = it.id, text = it.text) })

            return ResponseEntity(
                HttpHeaders().apply {
                    set(
                        "Location",
                        linksBuilder.self(newLegalRestriction.id).href
                    )
                },
                HttpStatus.CREATED
            )
        } catch (e: Exception) {
            print(e)
            throw e
        }
    }
}