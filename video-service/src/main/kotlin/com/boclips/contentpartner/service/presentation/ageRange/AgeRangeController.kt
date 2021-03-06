package com.boclips.contentpartner.service.presentation.ageRange

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.agerange.GetAgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.response.channel.AgeRangeResource
import com.boclips.videos.api.response.channel.AgeRangesResource
import com.boclips.videos.api.response.channel.AgeRangesWrapperResource
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
class AgeRangeController(
    private val createAgeRange: CreateAgeRange,
    private val ageRangeRepository: AgeRangeRepository,
    private val fetchAgeRange: GetAgeRange,
    private val ageRangeLinkBuilder: AgeRangeLinkBuilder,
    private val ageToResourceConverter: AgeRangeResourceConverter
) {
    @PostMapping
    fun post(@Valid @RequestBody createAgeRangeRequest: AgeRangeRequest): ResponseEntity<Void> {
        val ageRange = createAgeRange(createAgeRangeRequest)

        return ResponseEntity(
            HttpHeaders().apply {
                set(
                    "Location",
                    ageRangeLinkBuilder.self(ageRange.id.value).href
                )
            },
            HttpStatus.CREATED
        )
    }

    @GetMapping
    fun getAgeRanges(): AgeRangesResource {
        val ageRanges = ageRangeRepository.findAll()

        val resources = ageRanges.map {
            ageToResourceConverter.convert(it)
        }

        return AgeRangesResource(_embedded = AgeRangesWrapperResource(resources))
    }

    @GetMapping("/{id}")
    fun getAgeRange(@PathVariable("id") @NotBlank ageRangeId: String?): ResponseEntity<AgeRangeResource> {
        val ageRangeResource = fetchAgeRange(
            AgeRangeId(
                ageRangeId!!
            )
        )

        return ResponseEntity(ageToResourceConverter.convert(ageRangeResource), HttpStatus.OK)
    }
}
