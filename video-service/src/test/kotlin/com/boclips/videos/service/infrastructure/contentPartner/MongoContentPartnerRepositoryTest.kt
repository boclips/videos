package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
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
    fun `findById does not throw for invalid object id`() {
        val retrievedAsset = mongoContentPartnerRepository.findById(ContentPartnerId("invalid-hex-string"))

        assertThat(retrievedAsset).isNull()
    }

    @Test
    fun `find all by name filter`() {
        val contentPartnerIds = listOf(
            mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(name = "hello")
            ).contentPartnerId,
            mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(name = "hello")
            ).contentPartnerId
        )

        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(name = "good day")
        )

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ContentPartnerFilter.NameFilter(name = "hello")))

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).isEqualTo(contentPartnerIds)
    }

    @Test
    fun `find all by official filter`() {
        val officialContentPartnerId = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.PartnerCredit)
        ).contentPartnerId


        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
        ).contentPartnerId

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ContentPartnerFilter.OfficialFilter(official = true)))

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).containsExactly(officialContentPartnerId)
    }

    @Test
    fun `find all by accredited to youtube channel id`() {
        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.PartnerCredit)
        ).contentPartnerId

        val accreditedToYtChannelContentPartner = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
        ).contentPartnerId

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(
                listOf(
                    ContentPartnerFilter.AccreditedTo(
                        Credit.YoutubeCredit(
                            channelId = "123"
                        )
                    )
                )
            )

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).containsExactly(
            accreditedToYtChannelContentPartner
        )
    }

    @Test
    fun `find all with multiple filters`() {
        val toBeFoundContentPartnerId = mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "hello")
        ).contentPartnerId

        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "shwmae")
        ).contentPartnerId

        mongoContentPartnerRepository.create(
            TestFactories.createContentPartner(credit = Credit.PartnerCredit, name = "hello")
        ).contentPartnerId

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(
                listOf(
                    ContentPartnerFilter.OfficialFilter(official = false),
                    ContentPartnerFilter.NameFilter(name = "hello")
                )
            )

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).containsExactly(toBeFoundContentPartnerId)
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

    @Nested
    inner class OverridingDistributionMethods {
        @Test
        fun `replaces with stream`() {
            val contentPartner = mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(
                    distributionMethods = emptySet()
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceDistributionMethods(
                        contentPartnerId = contentPartner.contentPartnerId,
                        distributionMethods = setOf(DistributionMethod.STREAM)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(setOf(DistributionMethod.STREAM))
        }

        @Test
        fun `replaces with download`() {
            val contentPartner = mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(
                    distributionMethods = emptySet()
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceDistributionMethods(
                        contentPartnerId = contentPartner.contentPartnerId,
                        distributionMethods = setOf(DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(setOf(DistributionMethod.DOWNLOAD))
        }

        @Test
        fun `replaces with all`() {
            val contentPartner = mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(
                    distributionMethods = emptySet()
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceDistributionMethods(
                        contentPartnerId = contentPartner.contentPartnerId,
                        distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(DistributionMethod.ALL)
        }

        @Test
        fun `replaces with empty`() {
            val contentPartner = mongoContentPartnerRepository.create(
                TestFactories.createContentPartner(
                    distributionMethods = emptySet()
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceDistributionMethods(
                        contentPartnerId = contentPartner.contentPartnerId,
                        distributionMethods = emptySet()
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.distributionMethods).isEmpty()
        }
    }
}
