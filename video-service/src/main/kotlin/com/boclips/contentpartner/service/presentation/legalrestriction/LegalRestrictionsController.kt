package com.boclips.contentpartner.service.presentation.legalrestriction

import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindAllLegalRestrictions
import com.boclips.contentpartner.service.application.legalrestriction.FindLegalRestrictions
import com.boclips.contentpartner.service.presentation.converters.LegalRestrictionsToResourceConverter
import com.boclips.videos.api.response.contentpartner.LegalRestrictionResource
import com.boclips.videos.api.response.contentpartner.LegalRestrictionsResource
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
    private val findAllLegalRestrictions: FindAllLegalRestrictions,
    private val legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter
)  {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun post(@RequestParam text: String?): ResponseEntity<LegalRestrictionResource> {
        val legalRestriction = createLegalRestrictions(text = text)

        return ResponseEntity(legalRestrictionsToResourceConverter.convert(legalRestriction), HttpStatus.CREATED)
    }

    @GetMapping
    fun getAll(): ResponseEntity<LegalRestrictionsResource> {
        val legalRestrictions = findAllLegalRestrictions()

        return ResponseEntity(legalRestrictionsToResourceConverter.convert(legalRestrictions), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable("id") id: String): ResponseEntity<LegalRestrictionResource> {
        val resource = findLegalRestrictions(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(legalRestrictionsToResourceConverter.convert(resource), HttpStatus.OK)
    }
}
