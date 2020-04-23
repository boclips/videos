package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.contentpartner.service.domain.service.contentpartnercontract.ContractService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerDomainContext")
class DomainContext {

    @Bean
    fun eventConverter(): EventConverter {
        return EventConverter()
    }

    @Bean
    fun contractService(
        contractRepository: ContentPartnerContractRepository
    ): ContractService {
        return ContractService(contractRepository)
    }
}
