package com.boclips.videos.service.domain.model.contentPartner

interface ContentPartnerRepository {
    fun create(contentPartner: ContentPartner): ContentPartner
    fun findAll(): Iterable<ContentPartner>
    fun findAll(filters: List<ContentPartnerFilter>): Iterable<ContentPartner>
    fun findById(contentPartnerId: ContentPartnerId): ContentPartner?
    fun findByName(contentPartnerName: String): ContentPartner?
    fun update(updateCommands: List<ContentPartnerUpdateCommand>)
}