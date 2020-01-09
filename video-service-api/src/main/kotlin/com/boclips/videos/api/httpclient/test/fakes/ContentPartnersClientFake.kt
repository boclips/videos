package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.ContentPartnersClient
import com.boclips.videos.api.request.contentpartner.ContentPartnerFilterRequest
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerWrapperResource
import com.boclips.videos.api.response.contentpartner.ContentPartnersResource

class ContentPartnersClientFake : ContentPartnersClient, FakeClient<ContentPartnerResource> {
    private val database: MutableMap<String, ContentPartnerResource> = LinkedHashMap()
    private var id = 0

    override fun getContentPartners(contentPartnerFilterRequest: ContentPartnerFilterRequest): ContentPartnersResource {
        val contentPartners = if (contentPartnerFilterRequest.name != null) {
            database.values.toList().filter { it.name == contentPartnerFilterRequest.name }
        } else {
            database.values.toList()
        }

        return ContentPartnersResource(_embedded = ContentPartnerWrapperResource(contentPartners))
    }

    override fun create(createContentPartnerRequest: CreateContentPartnerRequest) {
        val id = "${id++}"
        database[id] = ContentPartnerResource(
            id = id,
            name = createContentPartnerRequest.name!!,
            currency = createContentPartnerRequest.currency,
            legalRestrictions = null,
            distributionMethods = setOf(),
            official = true
        )
    }

    override fun add(element: ContentPartnerResource): ContentPartnerResource {
        database[element.id] = element
        return element
    }

    override fun findAll(): List<ContentPartnerResource> {
        return database.values.toList()
    }

    override fun clear() {
        database.clear()
    }
}
