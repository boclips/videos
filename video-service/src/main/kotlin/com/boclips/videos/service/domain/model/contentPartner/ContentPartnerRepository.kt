package com.boclips.videos.service.domain.model.contentPartner

interface ContentPartnerRepository {
    fun create(contentPartner: ContentPartner): ContentPartner
    fun find(contentPartnerId: ContentPartnerId): ContentPartner?
}