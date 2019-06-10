package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateOrFindContentPartnerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Autowired
    lateinit var createOrFindContentPartner: CreateOrFindContentPartner

    @Test
    fun `creates a content partner if it does not exist`() {
        val contentPartner = createOrFindContentPartner("partner")

        assertThat(contentPartnerRepository.findByName(contentPartner.name)).isEqualTo(contentPartner)
    }


    @Test
    fun `returns already created content partner`() {
        val alreadyPersistedContentPartner = contentPartnerRepository.create(TestFactories.createContentPartner(name = "partner"))

        val contentPartner = createOrFindContentPartner("partner")

        assertThat(alreadyPersistedContentPartner).isEqualTo(contentPartner)
    }
}