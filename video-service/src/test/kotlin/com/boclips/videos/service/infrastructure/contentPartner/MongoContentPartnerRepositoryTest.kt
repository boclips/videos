package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class MongoContentPartnerRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoContentPartnerRespository: MongoContentPartnerRepository

    @Test
    fun `can create a content partner`() {
        val contentPartner = TestFactories.createContentPartner()

        val createdAsset = mongoContentPartnerRespository.create(contentPartner = contentPartner)

        assertThat(createdAsset).isEqualTo(contentPartner)
    }

    @Test
    fun find() {
        val originalContentPartner = mongoContentPartnerRespository.create(
            TestFactories.createContentPartner()
        )

        val retrievedAsset = mongoContentPartnerRespository.find(originalContentPartner.contentPartnerId)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun findByName() {
        val contentPartnerName = UUID.randomUUID().toString()
        val originalContentPartner = mongoContentPartnerRespository.create(
            TestFactories.createContentPartner(name = contentPartnerName)
        )

        val retrievedAsset = mongoContentPartnerRespository.findByName(contentPartnerName)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `updating an existing content partner`() {
        val originalContentPartner = mongoContentPartnerRespository.create(TestFactories.createContentPartner(name = "Old name", ageRange = AgeRange.bounded(min = 11, max = 14)))

        val replacementContentPartner = TestFactories.createContentPartner(id = originalContentPartner.contentPartnerId, name = "New name", ageRange = AgeRange.bounded(min = 9, max = 16))

        mongoContentPartnerRespository.update(contentPartner = replacementContentPartner)

        val updatedContentPartner = mongoContentPartnerRespository.findByName(contentPartnerName = "New name")!!

        assertThat(updatedContentPartner.ageRange.min()).isEqualTo(9)
        assertThat(updatedContentPartner.ageRange.max()).isEqualTo(16)

        assertThat(mongoContentPartnerRespository.findByName(originalContentPartner.name)).isNull()
    }

    @Test
    fun `creates a new content partner on an update if no content partner matches the id`() {
        val contentPartner = mongoContentPartnerRespository.update(TestFactories.createContentPartner())

        assertThat(mongoContentPartnerRespository.find(contentPartner.contentPartnerId)).isNotNull()
    }
}
