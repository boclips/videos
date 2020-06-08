package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.videos.api.response.contract.ContractRoyaltySplitResource

class ContractRoyaltySplitConverter {
    fun fromResource(royaltySplit: ContractRoyaltySplitResource): ContractRoyaltySplit {
        return royaltySplit.let {
            ContractRoyaltySplit(download = it.download, streaming = it.streaming)
        }
    }
}
