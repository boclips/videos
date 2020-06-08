package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.contract.Contract
import org.springframework.stereotype.Component

@Component
class SignContractDocument(
    private val contractSignedLinkProvider: SignedLinkProvider
) {
    operator fun invoke(contract: Contract): Contract =
        contract.copy(
            contractDocument = contract.contractDocument
                ?.let {
                    contractSignedLinkProvider.signedGetLink(it)
                }
        )
}
