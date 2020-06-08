package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource

class ContractRoyaltySplitConverter {
    fun fromResource(royaltySplit: ContentPartnerContractRoyaltySplitResource): ContractRoyaltySplit {
        return royaltySplit.let {
            ContractRoyaltySplit(download = it.download, streaming = it.streaming)
        }
    }
}
