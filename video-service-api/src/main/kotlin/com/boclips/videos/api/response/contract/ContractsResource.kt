package com.boclips.videos.api.response.contract

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel

class ContractsResource(
    var _embedded: ContractsWrapperResource,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var page: PagedModel.PageMetadata? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, Link>? = null
)

data class ContractsWrapperResource(
    val contracts: List<ContractResource>
)
