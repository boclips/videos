package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.contract.ContractController
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class ContractsLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val CONTRACT = "contract"
        const val CREATE_CONTRACTS = "createContracts"
        const val CONTRACTS = "contracts"
    }

    fun self(id: String): HateoasLink {
        val withSelfRel = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(ContractController::class.java).getContract(
                id
            )
        ).withSelfRel()

        return HateoasLink(href = withSelfRel.href, rel = withSelfRel.rel.value())
    }

    fun contractLink(id: String?): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTRACTS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContractController::class.java).getContract(
                    id
                )
            ).withRel(Rels.CONTRACT)
        }
    }

    fun contractsLink(): Link? {
        return getIfHasRole(UserRoles.VIEW_CONTRACTS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ContractController::class.java).getAll(
                    null, null
                )
            ).withRel(Rels.CONTRACTS)
        }
    }

    fun createContractLink(): Link? {
        return getIfHasRole(
            UserRoles.INSERT_CONTRACTS
        ) {
            Link.of(
                getContractsRoot()
                    .build()
                    .toUriString(),
                Rels.CREATE_CONTRACTS
            )
        }
    }

    fun createSignedUploadLink(): Link? =
        getIfHasRole(UserRoles.INSERT_CONTRACTS) {
            Link.of(
                getContractsRoot()
                    .build()
                    .toUriString()
                    .plus("/signed-upload-link"),
                "createContractsSignedUploadLink"
            )
        }

    private fun getContractsRoot() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/contracts")
        .replaceQueryParams(null)
}
