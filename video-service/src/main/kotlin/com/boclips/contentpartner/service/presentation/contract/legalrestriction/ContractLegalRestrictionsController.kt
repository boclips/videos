package com.boclips.contentpartner.service.presentation.contract.legalrestriction

import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.CreateContractLegalRestriction
import com.boclips.contentpartner.service.application.contentpartnercontract.legalrestrictions.FindAllContractLegalRestrictions
import com.boclips.contentpartner.service.presentation.converters.ContractLegalRestrictionsToResourceConverter
import com.boclips.videos.api.request.contract.legalrestrictions.CreateContractLegalRestrictionRequest
import com.boclips.videos.api.response.contract.legalrestrictions.ContractLegalRestrictionsResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/contract-legal-restrictions")
class ContractLegalRestrictionsController(
    private val fetchAll: FindAllContractLegalRestrictions,
    private val createLegalRestrictions: CreateContractLegalRestriction,
    private val toResourceConverter: ContractLegalRestrictionsToResourceConverter
)  {

    @GetMapping
    fun getAll(): ResponseEntity<ContractLegalRestrictionsResource> {
        val allLegalRestrictions = fetchAll()

        val convertedRestrictions = toResourceConverter(allLegalRestrictions)

        return ResponseEntity(convertedRestrictions, HttpStatus.OK)
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateContractLegalRestrictionRequest
    ): ResponseEntity<Void> {
        createLegalRestrictions(request)
        return ResponseEntity(HttpStatus.CREATED)
    }
}