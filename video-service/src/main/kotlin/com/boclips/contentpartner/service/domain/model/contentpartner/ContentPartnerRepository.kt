package com.boclips.contentpartner.service.domain.model.contentpartner

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId

interface ContentPartnerRepository {
    fun create(contentPartner: ContentPartner): ContentPartner
    fun findAll(): Iterable<ContentPartner>
    fun findAll(filters: List<ContentPartnerFilter>): Iterable<ContentPartner>
    fun findById(contentPartnerId: ContentPartnerId): ContentPartner?
    fun findByContractId(contractId: ContentPartnerContractId): List<ContentPartner>
    fun findByName(query: String): List<ContentPartner>
    fun update(updateCommands: List<ContentPartnerUpdateCommand>)
}
