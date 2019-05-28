package com.boclips.videos.service.domain.model.contentPartner

interface ContentPartnerRepository {
    fun create(contentPartner: ContentPartner): ContentPartner
    fun find(contentPartnerId: ContentPartnerId): ContentPartner?
    fun findByName(contentPartnerName: String): ContentPartner?
    fun update(contentPartner: ContentPartner): ContentPartner
}