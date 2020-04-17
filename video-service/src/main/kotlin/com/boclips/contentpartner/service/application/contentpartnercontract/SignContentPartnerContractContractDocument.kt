package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import org.springframework.stereotype.Component

@Component
class SignContentPartnerContractContractDocument(
    private val contractSignedLinkProvider: SignedLinkProvider
) {
    operator fun invoke(contract: ContentPartnerContract): ContentPartnerContract =
        contract.copy(
            contractDocument = contract.contractDocument
                ?.let {
                    contractSignedLinkProvider.signedGetLink(it)
                }
        )
}