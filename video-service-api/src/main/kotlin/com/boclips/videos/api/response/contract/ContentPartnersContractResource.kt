package com.boclips.videos.api.response.contract

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel

class ContentPartnerContractsResource(
    var _embedded: ContentPartnerContractsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedModel.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class ContentPartnerContractsWrapperResource(
    val contracts: List<ContentPartnerContractResource>
)

