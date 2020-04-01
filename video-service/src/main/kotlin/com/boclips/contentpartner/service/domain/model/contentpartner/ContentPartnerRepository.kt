package com.boclips.contentpartner.service.domain.model.contentpartner

interface ContentPartnerRepository {
    fun create(contentPartner: ContentPartner): ContentPartner
    fun findAll(): Iterable<ContentPartner>
    fun findAll(filters: List<ContentPartnerFilter>): Iterable<ContentPartner>
    fun findById(contentPartnerId: ContentPartnerId): ContentPartner?
    fun update(updateCommands: List<ContentPartnerUpdateCommand>)
}
