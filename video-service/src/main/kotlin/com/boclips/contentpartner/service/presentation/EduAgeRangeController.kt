package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CreateEduAgeRange
import com.boclips.contentpartner.service.application.GetEduAgeRange
import com.boclips.contentpartner.service.application.EduAgeRangeResourceConverter
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.EduAgeRangeRepository
import com.boclips.videos.api.request.contentpartner.EduAgeRangeRequest
import com.boclips.videos.api.response.contentpartner.EduAgeRangeResource
import com.boclips.videos.api.response.contentpartner.EduAgeRangesResource
import com.boclips.videos.api.response.contentpartner.EduAgeRangesWrapperResource
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
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/age-ranges")
class EduAgeRangeController(
    private val createEduAgeRange: CreateEduAgeRange,
    private val eduAgeRangeRepository: EduAgeRangeRepository,
    private val fetchEduAgeRange: GetEduAgeRange,
    private val eduAgeRangeLinkBuilder: EduAgeRangeLinkBuilder,
    private val eduAgeToResourceConverter: EduAgeRangeResourceConverter
) {
    @PostMapping
    fun postEduAgeRange(@Valid @RequestBody createEduAgeRangeRequest: EduAgeRangeRequest): ResponseEntity<Void> {
        val eduAgeRage = createEduAgeRange(createEduAgeRangeRequest)

        return ResponseEntity(
            HttpHeaders().apply {
                set(
                    "Location",
                    eduAgeRangeLinkBuilder.self(eduAgeRage.id.value).href
                )
            },
            HttpStatus.CREATED
        )
    }

    @GetMapping
    fun getEduAgeRanges(): EduAgeRangesResource {
        val ageRanges = eduAgeRangeRepository.findAll()

        val resources = ageRanges.map {
            eduAgeToResourceConverter.convert(it)
        }

        return EduAgeRangesResource(_embedded = EduAgeRangesWrapperResource(resources))
    }

    @GetMapping("/{id}")
    fun getEduAgeRange(@PathVariable("id") @NotBlank eduAgeRangeId: String?): ResponseEntity<EduAgeRangeResource> {
        val ageRangeResource = fetchEduAgeRange(EduAgeRangeId(eduAgeRangeId!!))

        return ResponseEntity(eduAgeToResourceConverter.convert(ageRangeResource), HttpStatus.OK)
    }
}