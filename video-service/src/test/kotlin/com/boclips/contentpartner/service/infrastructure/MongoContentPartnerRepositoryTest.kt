package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.ContentPartnerFilter
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.TestFactories
import com.boclips.contentpartner.service.testsupport.TestFactories.createContentPartner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class MongoContentPartnerRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoContentPartnerRepository: MongoContentPartnerRepository

    @Test
    fun `can create a content partner`() {
        val contentPartner = createContentPartner()

        val createdAsset = mongoContentPartnerRepository.create(contentPartner = contentPartner)

        assertThat(createdAsset.contentPartnerId.value).isEqualTo(contentPartner.contentPartnerId.value)
    }

    @Test
    fun find() {
        val originalContentPartner = mongoContentPartnerRepository.create(
            createContentPartner()
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.contentPartnerId)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `findById does not throw for invalid object id`() {
        val retrievedAsset = mongoContentPartnerRepository.findById(
            ContentPartnerId(
                "invalid-hex-string"
            )
        )

        assertThat(retrievedAsset).isNull()
    }

    @Test
    fun `find all by name filter`() {
        val contentPartnerIds = listOf(
            mongoContentPartnerRepository.create(
                createContentPartner(name = "hello")
            ).contentPartnerId,
            mongoContentPartnerRepository.create(
                createContentPartner(name = "hello")
            ).contentPartnerId
        )

        mongoContentPartnerRepository.create(
            createContentPartner(name = "good day")
        )

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ContentPartnerFilter.NameFilter(name = "hello")))

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).isEqualTo(contentPartnerIds)
    }

    @Test
    fun `find all by official filter`() {
        val officialContentPartnerId = mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit)
        ).contentPartnerId


        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
        ).contentPartnerId

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ContentPartnerFilter.OfficialFilter(official = true)))

        assertThat(retrievedContentPartners.map { it.contentPartnerId }).containsExactly(officialContentPartnerId)
    }

    @Test
    fun `find all by accredited to youtube channel id`() {
        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit)
        ).contentPartnerId

        val accreditedToYtChannelContentPartner = mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
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
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "hello")
        ).contentPartnerId

        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "shwmae")
        ).contentPartnerId

        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit, name = "hello")
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
            createContentPartner(
                credit = Credit.YoutubeCredit(channelId = "123")
            )
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.contentPartnerId)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `find all content partners`() {
        mongoContentPartnerRepository.create(
            createContentPartner(name = "my bloody valentine")
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
            createContentPartner(name = "my bloody valentine")
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
            createContentPartner(
                ageRanges = AgeRangeBuckets(emptyList())
            )
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceAgeRanges(
                    contentPartnerId = contentPartner.contentPartnerId,
                    ageRangeBuckets = AgeRangeBuckets(listOf(TestFactories.createAgeRange(min = 10, max = 20)))
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
        assertThat(updatedAsset.ageRangeBuckets.min).isEqualTo(10)
        assertThat(updatedAsset.ageRangeBuckets.max).isEqualTo(20)
    }

    @Test
    fun `replace legal restrictions`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val legalRestrictions = LegalRestriction(
            id = LegalRestrictionsId(TestFactories.aValidId()),
            text = "New restrictions"
        )

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceLegalRestrictions(
                    contentPartner.contentPartnerId,
                    legalRestrictions
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.legalRestriction).isEqualTo(legalRestrictions)
    }

    @Test
    fun `replace curriculumAligned`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val curriculumAligned = "this is a curriculum"

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceCurriculumAligned(
                    contentPartner.contentPartnerId,
                    curriculumAligned
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.pedagogyInformation?.curriculumAligned).isEqualTo(curriculumAligned)
    }

    @Test
    fun `replace isTranscriptProvided`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val isTranscriptProvided = true

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided(
                    contentPartner.contentPartnerId,
                    isTranscriptProvided
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.pedagogyInformation?.isTranscriptProvided).isEqualTo(isTranscriptProvided)
    }

    @Test
    fun `replace educational resources`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val educationalResources = "this is a educational resource"

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceEducationalResources(
                    contentPartner.contentPartnerId,
                    educationalResources
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.pedagogyInformation?.educationalResources).isEqualTo(educationalResources)
    }

    @Test
    fun `replace best for tags`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val bestForTags = listOf("123", "456")

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceBestForTags(
                    contentPartner.contentPartnerId,
                    bestForTags
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.pedagogyInformation?.bestForTags).isEqualTo(bestForTags)
    }

    @Test
    fun `replace subjects`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val subjects = listOf("subject 1", "subject 2")

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceSubjects(
                    contentPartner.contentPartnerId,
                    subjects
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.pedagogyInformation?.subjects).isEqualTo(subjects)
    }

    @Test
    fun `replace ingest details`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner(ingest = ManualIngest))

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceIngestDetails(
                    contentPartner.contentPartnerId,
                    YoutubeScrapeIngest("http://youtube.com/channel")
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.ingest).isEqualTo(YoutubeScrapeIngest("http://youtube.com/channel"))
    }

    @Test
    fun `replace delivery frequency`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner(deliveryFrequency = Period.ofMonths(1)))

        mongoContentPartnerRepository.update(
            listOf(
                ContentPartnerUpdateCommand.ReplaceDeliveryFrequency(
                    contentPartner.contentPartnerId,
                    Period.ofYears(1)
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)
        assertThat(updatedContentPartner?.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Nested
    inner class OverridingDistributionMethods {
        @Test
        fun `replaces with stream`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
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
                createContentPartner(
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
                createContentPartner(
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
                createContentPartner(
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

        @Test
        fun `replaces educational resources`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(educationalResources = "this is a resource")
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceEducationalResources(
                        contentPartner.contentPartnerId, "New Resource"
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.pedagogyInformation?.educationalResources).isEqualTo("New Resource")
        }

        @Test
        fun `replaces best for tags`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(bestForTags = listOf("123", "345"))
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceBestForTags(
                        contentPartner.contentPartnerId, listOf("555", "666")
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.pedagogyInformation?.bestForTags).containsExactlyInAnyOrder("555", "666")
        }

        @Test
        fun `replaces subjects`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(subjects = listOf("subject 1", "subject 2"))
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ContentPartnerUpdateCommand.ReplaceSubjects(
                        contentPartner.contentPartnerId, listOf("subject 3", "subject 4")
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.contentPartnerId)!!
            assertThat(updatedAsset.pedagogyInformation?.subjects).containsExactlyInAnyOrder("subject 3", "subject 4")
        }
    }
}
