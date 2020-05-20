package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.Credit
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class MongoChannelRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoContentPartnerRepository: MongoContentPartnerRepository

    @Test
    fun `can create a content partner`() {
        val contentPartner = createContentPartner()

        val createdAsset = mongoContentPartnerRepository.create(channel = contentPartner)

        assertThat(createdAsset.id.value).isEqualTo(contentPartner.id.value)
    }

    @Test
    fun find() {
        val originalContentPartner = mongoContentPartnerRepository.create(
            createContentPartner()
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.id)

        assertThat(retrievedAsset).isEqualTo(originalContentPartner)
    }

    @Test
    fun `findById does not throw for invalid object id`() {
        val retrievedAsset = mongoContentPartnerRepository.findById(
            ChannelId(
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
            ).id,
            mongoContentPartnerRepository.create(
                createContentPartner(name = "hello")
            ).id
        )

        mongoContentPartnerRepository.create(
            createContentPartner(name = "good day")
        )

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ChannelFilter.NameFilter(name = "hello")))

        assertThat(retrievedContentPartners.map { it.id }).isEqualTo(contentPartnerIds)
    }

    @Test
    fun `find all by official filter`() {
        val officialContentPartnerId = mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit)
        ).id


        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
        ).id

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(listOf(ChannelFilter.OfficialFilter(official = true)))

        assertThat(retrievedContentPartners.map { it.id }).containsExactly(officialContentPartnerId)
    }

    @Test
    fun `find all by accredited to youtube channel id`() {
        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit)
        ).id

        val accreditedToYtChannelContentPartner = mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"))
        ).id

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(
                listOf(
                    ChannelFilter.AccreditedTo(
                        Credit.YoutubeCredit(
                            channelId = "123"
                        )
                    )
                )
            )

        assertThat(retrievedContentPartners.map { it.id }).containsExactly(
            accreditedToYtChannelContentPartner
        )
    }

    @Test
    fun `find all with multiple filters`() {
        val toBeFoundContentPartnerId = mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "hello")
        ).id

        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.YoutubeCredit(channelId = "123"), name = "shwmae")
        ).id

        mongoContentPartnerRepository.create(
            createContentPartner(credit = Credit.PartnerCredit, name = "hello")
        ).id

        val retrievedContentPartners =
            mongoContentPartnerRepository.findAll(
                listOf(
                    ChannelFilter.OfficialFilter(official = false),
                    ChannelFilter.NameFilter(name = "hello")
                )
            )

        assertThat(retrievedContentPartners.map { it.id }).containsExactly(toBeFoundContentPartnerId)
    }

    @Test
    fun `find by youtube channel name`() {
        val originalContentPartner = mongoContentPartnerRepository.create(
            createContentPartner(
                credit = Credit.YoutubeCredit(channelId = "123")
            )
        )

        val retrievedAsset = mongoContentPartnerRepository.findById(originalContentPartner.id)

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
    fun `find by matching names ignoring casing`() {
        mongoContentPartnerRepository.create(createContentPartner(name = "TED"))
        mongoContentPartnerRepository.create(createContentPartner(name = "TED"))
        mongoContentPartnerRepository.create(createContentPartner(name = "TED-Ed"))
        mongoContentPartnerRepository.create(createContentPartner(name = "BBC"))

        val results = mongoContentPartnerRepository.findByName("ted")

        assertThat(results).hasSize(2)
        assertThat(results[0].name).isEqualTo("TED")
        assertThat(results[1].name).isEqualTo("TED-Ed")
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
                ChannelUpdateCommand.ReplaceName(
                    channelId = contentPartner.id,
                    name = "new name"
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedAsset?.name).isEqualTo("new name")
    }

    @Test
    fun `replaces age range`() {
        val contentPartner = mongoContentPartnerRepository.create(
            createContentPartner(
                pedagogyInformation = PedagogyInformation(
                    ageRangeBuckets = AgeRangeBuckets(
                        emptyList()
                    )
                )
            )
        )

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceAgeRanges(
                    channelId = contentPartner.id,
                    ageRangeBuckets = AgeRangeBuckets(
                        listOf(ContentPartnerFactory.createAgeRange(min = 10, max = 20))
                    )
                )
            )
        )

        val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
        assertThat(updatedAsset.pedagogyInformation?.ageRangeBuckets?.min).isEqualTo(10)
        assertThat(updatedAsset.pedagogyInformation?.ageRangeBuckets?.max).isEqualTo(20)
    }

    @Test
    fun `replace legal restrictions`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val legalRestrictions =
            LegalRestriction(
                id = LegalRestrictionsId(
                    ContentPartnerFactory.aValidId()
                ),
                text = "New restrictions"
            )

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceLegalRestrictions(
                    contentPartner.id,
                    legalRestrictions
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.legalRestriction).isEqualTo(legalRestrictions)
    }

    @Test
    fun `replace curriculumAligned`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val curriculumAligned = "this is a curriculum"

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceCurriculumAligned(
                    contentPartner.id,
                    curriculumAligned
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.pedagogyInformation?.curriculumAligned).isEqualTo(curriculumAligned)
    }

    @Test
    fun `replace isTranscriptProvided`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val isTranscriptProvided = true

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceIsTranscriptProvided(
                    contentPartner.id,
                    isTranscriptProvided
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.pedagogyInformation?.isTranscriptProvided).isEqualTo(isTranscriptProvided)
    }

    @Test
    fun `replace educational resources`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val educationalResources = "this is a educational resource"

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceEducationalResources(
                    contentPartner.id,
                    educationalResources
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.pedagogyInformation?.educationalResources).isEqualTo(educationalResources)
    }

    @Test
    fun `replace best for tags`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val bestForTags = listOf("123", "456")

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceBestForTags(
                    contentPartner.id,
                    bestForTags
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.pedagogyInformation?.bestForTags).isEqualTo(bestForTags)
    }

    @Test
    fun `replace subjects`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner())
        val subjects = listOf("subject 1", "subject 2")

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceSubjects(
                    contentPartner.id,
                    subjects
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.pedagogyInformation?.subjects).isEqualTo(subjects)
    }

    @Test
    fun `replace ingest details`() {
        val contentPartner = mongoContentPartnerRepository.create(createContentPartner(ingest = ManualIngest))

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceIngestDetails(
                    contentPartner.id,
                    YoutubeScrapeIngest(
                        listOf("http://youtube.com/channel")
                    )
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
        assertThat(updatedContentPartner?.ingest).isEqualTo(
            YoutubeScrapeIngest(
                listOf("http://youtube.com/channel")
            )
        )
    }

    @Test
    fun `replace delivery frequency`() {
        val contentPartner =
            mongoContentPartnerRepository.create(createContentPartner(deliveryFrequency = Period.ofMonths(1)))

        mongoContentPartnerRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceDeliveryFrequency(
                    contentPartner.id,
                    Period.ofYears(1)
                )
            )
        )

        val updatedContentPartner = mongoContentPartnerRepository.findById(contentPartner.id)
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
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = contentPartner.id,
                        distributionMethods = setOf(DistributionMethod.STREAM)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
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
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = contentPartner.id,
                        distributionMethods = setOf(DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
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
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = contentPartner.id,
                        distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
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
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = contentPartner.id,
                        distributionMethods = emptySet()
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
            assertThat(updatedAsset.distributionMethods).isEmpty()
        }
    }

    @Nested
    inner class UpdatingContentPartners {
        @Test
        fun `replaces educational resources`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(
                        educationalResources = "this is a resource"
                    )
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceEducationalResources(
                        contentPartner.id, "New Resource"
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
            assertThat(updatedAsset.pedagogyInformation?.educationalResources).isEqualTo("New Resource")
        }

        @Test
        fun `replaces best for tags`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(
                        bestForTags = listOf("123", "345")
                    )
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceBestForTags(
                        contentPartner.id, listOf("555", "666")
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
            assertThat(updatedAsset.pedagogyInformation?.bestForTags).containsExactlyInAnyOrder("555", "666")
        }

        @Test
        fun `replaces subjects`() {
            val contentPartner = mongoContentPartnerRepository.create(
                createContentPartner(
                    pedagogyInformation = PedagogyInformation(
                        subjects = listOf("subject 1", "subject 2")
                    )
                )
            )

            mongoContentPartnerRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceSubjects(
                        contentPartner.id, listOf("subject 3", "subject 4")
                    )
                )
            )

            val updatedAsset = mongoContentPartnerRepository.findById(contentPartner.id)!!
            assertThat(updatedAsset.pedagogyInformation?.subjects).containsExactlyInAnyOrder("subject 3", "subject 4")
        }

        @Test
        fun `replaces contracts`() {
            val oldContract = ContentPartnerContractFactory.sample(contentPartnerName = "old")
            val contentPartner = mongoContentPartnerRepository.create(createContentPartner(contract = oldContract))

            val newContract = ContentPartnerContractFactory.sample(contentPartnerName = "new")
            mongoContentPartnerRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceContract(
                        channelId = contentPartner.id,
                        contract = newContract
                    )
                )
            )

            val updated = mongoContentPartnerRepository.findById(contentPartner.id)!!
            assertThat(updated.contract).isEqualTo(newContract)
        }

        @Nested
        inner class FindByContractId {
            @Test
            fun `can find one by contract id`() {
                val contract = ContentPartnerContractFactory.sample()
                val contentPartner = mongoContentPartnerRepository.create(
                    createContentPartner(
                        contract = contract
                    )
                )

                val retrievedContentPartners = mongoContentPartnerRepository.findByContractId(contractId = contract.id)

                assertThat(retrievedContentPartners).containsExactly(contentPartner)
            }

            @Test
            fun `can multiple by contract id`() {
                val contract = ContentPartnerContractFactory.sample()
                val firstContentPartner = mongoContentPartnerRepository.create(
                    createContentPartner(
                        contract = contract
                    )
                )

                val secondContentPartner = mongoContentPartnerRepository.create(
                    createContentPartner(
                        contract = contract
                    )
                )

                mongoContentPartnerRepository.create(
                    createContentPartner(
                        contract = null
                    )
                )

                val retrievedContentPartners = mongoContentPartnerRepository.findByContractId(contractId = contract.id)

                assertThat(retrievedContentPartners).containsExactlyInAnyOrder(
                    firstContentPartner,
                    secondContentPartner
                )
            }
        }
    }
}
