package com.boclips.videos.api.response.contract.legalrestrictions

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

data class ContractLegalRestrictionsResource(
    var _embedded: LegalRestrictionsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class LegalRestrictionsWrapperResource(
    val restrictions: List<ContractLegalRestrictionResource>
)

data class ContractLegalRestrictionResource(
    val id: String,
    val text: String
)
