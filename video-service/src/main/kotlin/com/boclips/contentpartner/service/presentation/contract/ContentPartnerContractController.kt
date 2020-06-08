package com.boclips.contentpartner.service.presentation.contract

import com.boclips.contentpartner.service.application.contract.CreateContract
import com.boclips.contentpartner.service.application.contract.GetContract
import com.boclips.contentpartner.service.application.contract.GetContracts
import com.boclips.contentpartner.service.application.contract.SignContractDocument
import com.boclips.contentpartner.service.application.contract.UpdateContract
import com.boclips.contentpartner.service.application.exceptions.ContractNotFoundException
import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContractsLinkBuilder
import com.boclips.videos.api.request.SignedLinkRequest
import com.boclips.videos.api.request.contract.CreateContractRequest
import com.boclips.videos.api.request.contract.UpdateContractRequest
import com.boclips.videos.api.response.contract.ContentPartnerContractResource
import com.boclips.videos.api.response.contract.ContentPartnerContractsResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/v1/content-partner-contracts")
class ContentPartnerContractController(
    private val fetchOne: GetContract,
    private val fetch: GetContracts,
    private val create: CreateContract,
    private val update: UpdateContract,
    private val signContractDocument: SignContractDocument,
    private val toResourceConverter: ContractToResourceConverter,
    private val linksBuilder: ContractsLinkBuilder,
    private val contractSignedLinkProvider: SignedLinkProvider
) {
    @GetMapping
    fun getAll(
        @RequestParam(name = "size", required = false) size: Int?,
        @RequestParam(name = "page", required = false) page: Int?
    ): ResponseEntity<ContentPartnerContractsResource> {
        val resources = fetch(page = page, size = size).let { toResourceConverter.convert(it) }

        return ResponseEntity(resources, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getContentPartnerContract(@PathVariable("id") @NotBlank id: String?): ResponseEntity<ContentPartnerContractResource> {
        val resource = fetchOne(
            ContractId(
                id!!
            )
        )
            ?.let { signContractDocument(it) }
            ?.let { toResourceConverter.convert(it) }
            ?: throw ContractNotFoundException("No content partner contract found with id=$id")

        return ResponseEntity(resource, HttpStatus.OK)
    }

    @PostMapping
    fun postContentPartnerContract(@Valid @RequestBody request: CreateContractRequest): ResponseEntity<Void> {
        try {
            val contractId = create(request)

            return ResponseEntity(
                HttpHeaders().apply {
                    set(
                        "Location",
                        linksBuilder.self(contractId.id.value).href
                    )
                }, HttpStatus.CREATED
            )
        } catch (e: Exception) {
            print(e)
            throw e
        }
    }

    @PatchMapping("/{id}")
    fun patchContract(
        @PathVariable("id") contractId: String,
        @Valid @RequestBody updateContentPartnerContract: UpdateContractRequest
    ): ResponseEntity<Void> {
        update(
            contractId, updateContentPartnerContract
        )

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/signed-upload-link")
    fun signedLink(
        @RequestBody signedLinkRequest: SignedLinkRequest
    ): ResponseEntity<Void> {
        val link = contractSignedLinkProvider.signedPutLink(signedLinkRequest.filename)
        return ResponseEntity(HttpHeaders().apply {
            set(
                "Location",
                link.toString()
            )
        }, HttpStatus.NO_CONTENT)
    }
}
