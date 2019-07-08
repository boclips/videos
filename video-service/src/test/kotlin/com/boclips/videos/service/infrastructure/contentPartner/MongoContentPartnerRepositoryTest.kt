package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class MongoContentPartnerRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoContentPartnerRepository: MongoContentPartnerRepository

    @Test
    fun `can create a content partner`() {
        val contentPartner = TestFactories.createContentPartner()

        val createdAsset = mongoContentPartnerRepository.create(contentPartner = contentPartner)

        assertThat(createdAsset).isEqualTo(contentPartner)
    }

    @Test
    fun find() {
        val originalContentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner()
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.contentPartnerId)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `find by youtube channel name`() {
        val originalContentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(
                credit = Credit.YoutubeCredit(channelId = "123")
            )
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.contentPartnerId)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun findByName() {
        val contentPartnerName = UUID.randomUUID().toString()
        val originalContentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(name = contentPartnerName)
        )

        val retrievedAsset = mongoContentPartnerRepository.findByName(contentPartnerName)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `find all content partners`() {
        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(name = "my bloody valentine")
        )

        val retrievedAsset = mongoContentPartnerRepository.findAll()

        assertThat(retrievedAsset).hasSize(1)
        assertThat(retrievedAsset.first()!!.name).isEqualTo("my bloody valentine")
    }

    @Test
    fun `does not update given an empty list of update commands`() {
        Assertions.assertDoesNotThrow { mongoContentPartnerRepository.update(listOf()) }
    }

    @Test
    fun `replaces name`() {
        val contentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(name = "my bloody valentine")
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceName(
                    contentPartnerId = contentPartner.contentPartnerId,
                    name = "new name"
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedAsset?.name).isEqualTo("new name")
    }

    @Test
    fun `replaces age range`() {
        val contentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(
                ageRange = AgeRange.unbounded()
            )
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceAgeRange(
                    contentPartnerId = contentPartner.contentPartnerId,
                    ageRange = AgeRange.bounded(10, 20)
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
        assertThat(updatedAsset.ageRange.min()).isEqualTo(10)
        assertThat(updatedAsset.ageRange.max()).isEqualTo(20)
    }

    @Test
    fun `sets searchability`() {
        val contentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(
                searchable = false
            )
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.SetSearchability(
                    contentPartnerId = contentPartner.contentPartnerId,
                    searchable = true
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
        assertThat(updatedAsset.searchable).isEqualTo(true)
    }

    @Test
    fun `sets hidden delivery methods`() {
        val contentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(
                hiddenFromSearchForDeliveryMethods = emptySet()
            )
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.SetHiddenDeliveryMethods(
                    contentPartnerId = contentPartner.contentPartnerId,
                    methods = setOf(DeliveryMethod.STREAM)
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
        assertThat(updatedAsset.hiddenFromSearchForDeliveryMethods).isEqualTo(setOf(DeliveryMethod.STREAM))
    }
}
