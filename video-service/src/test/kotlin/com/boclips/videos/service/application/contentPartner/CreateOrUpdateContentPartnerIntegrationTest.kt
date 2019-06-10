package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateOrUpdateContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var createOrUpdateContentPartner: CreateOrUpdateContentPartner

    @Test
    fun `creates a content partner if it does not exist`() {
        val contentPartner =
            createOrUpdateContentPartner(contentPartnerId = ContentPartnerId(value = "partnerId"), provider = "hello")

        assertThat(contentPartnerRepository.findById(contentPartner.contentPartnerId)).isEqualTo(contentPartner)
    }

    @Test
    fun `updates already created content partner`() {
        contentPartnerRepository.create(TestFactories.createContentPartner(id = ContentPartnerId(value = "id")))

        val updatedContentPartner = createOrUpdateContentPartner(ContentPartnerId("id"), provider = "newName")

        val persistedContentPartner = contentPartnerRepository.findById(updatedContentPartner.contentPartnerId)

        assertThat(persistedContentPartner!!.name).isEqualTo("newName")
        assertThat(persistedContentPartner).isEqualTo(updatedContentPartner)
    }
}