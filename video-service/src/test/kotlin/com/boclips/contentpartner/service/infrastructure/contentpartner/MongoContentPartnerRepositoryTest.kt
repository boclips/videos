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
import com.boclips.contentpartner.service.infrastructure.channel.MongoChannelRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class MongoChannelRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoChannelRepository: MongoChannelRepository

    @Test
    fun `can create a content partner`() {
        val channel = createChannel()

        val createdAsset = mongoChannelRepository.create(channel = channel)

        assertThat(createdAsset.id.value).isEqualTo(channel.id.value)
    }

    @Test
    fun find() {
        val originalChannel = mongoChannelRepository.create(
            createChannel()
        )

        val retrievedAsset = mongoChannelRepository.findById(originalChannel.id)

        assertThat(retrievedAsset).isEqualTo(originalChannel)
    }

    @Test
    fun `findById does not throw for invalid object id`() {
        val retrievedAsset = mongoChannelRepository.findById(
            ChannelId(
                "invalid-hex-string"
            )
        )

        assertThat(retrievedAsset).isNull()
    }

    @Test
    fun `find all by name filter`() {
        val channelIds = listOf(
            mongoChannelRepository.create(
                createChannel(name = "hello")
            ).id,
            mongoChannelRepository.create(
                createChannel(name = "hello")
            ).id
        )

        mongoChannelRepository.create(
            createChannel(name = "good day")
        )

        val retrievedChannel =
            mongoChannelRepository.findAll(listOf(ChannelFilter.NameFilter(name = "hello")))

        assertThat(retrievedChannel.map { it.id }).isEqualTo(channelIds)
    }

    @Test
    fun `find all by official filter`() {
        val officialChannelId = mongoChannelRepository.create(
            createChannel(credit = Credit.PartnerCredit)
        ).id


        mongoChannelRepository.create(
            createChannel(credit = Credit.YoutubeCredit(channelId = "123"))
        ).id

        val retrievedChannels =
            mongoChannelRepository.findAll(listOf(ChannelFilter.OfficialFilter(official = true)))

        assertThat(retrievedChannels.map { it.id }).containsExactly(officialChannelId)
    }

    @Test
    fun `find all by accredited to youtube channel id`() {
        mongoChannelRepository.create(
            createChannel(credit = Credit.PartnerCredit)
        ).id

        val accreditedToYtChannelId = mongoChannelRepository.create(
            createChannel(credit = Credit.YoutubeCredit(channelId = "123"))
        ).id

        val retrievedChannels =
            mongoChannelRepository.findAll(
                listOf(
                    ChannelFilter.AccreditedTo(
                        Credit.YoutubeCredit(
                            channelId = "123"
                        )
                    )
                )
            )

        assertThat(retrievedChannels.map { it.id }).containsExactly(
            accreditedToYtChannelId
        )
    }

    @Test
    fun `find all with multiple filters`() {
        val toBeFoundChannelId = mongoChannelRepository.create(
            createChannel(credit = Credit.YoutubeCredit(channelId = "123"), name = "hello")
        ).id

        mongoChannelRepository.create(
            createChannel(credit = Credit.YoutubeCredit(channelId = "123"), name = "shwmae")
        ).id

        mongoChannelRepository.create(
            createChannel(credit = Credit.PartnerCredit, name = "hello")
        ).id

        val retrievedChannels =
            mongoChannelRepository.findAll(
                listOf(
                    ChannelFilter.OfficialFilter(official = false),
                    ChannelFilter.NameFilter(name = "hello")
                )
            )

        assertThat(retrievedChannels.map { it.id }).containsExactly(toBeFoundChannelId)
    }

    @Test
    fun `find by youtube channel name`() {
        val originalChannel = mongoChannelRepository.create(
            createChannel(
                credit = Credit.YoutubeCredit(channelId = "123")
            )
        )

        val retrievedAsset = mongoChannelRepository.findById(originalChannel.id)

        assertThat(retrievedAsset).isEqualTo(originalChannel)
    }

    @Test
    fun `find all channel`() {
        mongoChannelRepository.create(
            createChannel(name = "my bloody valentine")
        )

        val retrievedAsset = mongoChannelRepository.findAll()

        assertThat(retrievedAsset).hasSize(1)
        assertThat(retrievedAsset.first()!!.name).isEqualTo("my bloody valentine")
    }

    @Test
    fun `find by matching names ignoring casing`() {
        mongoChannelRepository.create(createChannel(name = "TED"))
        mongoChannelRepository.create(createChannel(name = "TED"))
        mongoChannelRepository.create(createChannel(name = "TED-Ed"))
        mongoChannelRepository.create(createChannel(name = "BBC"))

        val results = mongoChannelRepository.findByName("ted")

        assertThat(results).hasSize(2)
        assertThat(results[0].name).isEqualTo("TED")
        assertThat(results[1].name).isEqualTo("TED-Ed")
    }

    @Test
    fun `find by name when string contains regex special chars`() {
        mongoChannelRepository.create(createChannel(name = "Creative Conspiracy (CC)"))

        val results = mongoChannelRepository.findByName("Creative Conspiracy (CC)")

        assertThat(results).hasSize(1)
    }

    @Test
    fun `does not update given an empty list of update commands`() {
        Assertions.assertDoesNotThrow { mongoChannelRepository.update(listOf()) }
    }

    @Test
    fun `replaces name`() {
        val channel = mongoChannelRepository.create(
            createChannel(name = "my bloody valentine")
        )

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceName(
                    channelId = channel.id,
                    name = "new name"
                )
            )
        )

        val updatedAsset = mongoChannelRepository.findById(channel.id)
        assertThat(updatedAsset?.name).isEqualTo("new name")
    }

    @Test
    fun `replaces age range`() {
        val channel = mongoChannelRepository.create(
            createChannel(
                pedagogyInformation = PedagogyInformation(
                    ageRangeBuckets = AgeRangeBuckets(
                        emptyList()
                    )
                )
            )
        )

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceAgeRanges(
                    channelId = channel.id,
                    ageRangeBuckets = AgeRangeBuckets(
                        listOf(ChannelFactory.createAgeRange(min = 10, max = 20))
                    )
                )
            )
        )

        val updatedAsset = mongoChannelRepository.findById(channel.id)!!
        assertThat(updatedAsset.pedagogyInformation?.ageRangeBuckets?.min).isEqualTo(10)
        assertThat(updatedAsset.pedagogyInformation?.ageRangeBuckets?.max).isEqualTo(20)
    }

    @Test
    fun `replace legal restrictions`() {
        val channel = mongoChannelRepository.create(createChannel())
        val legalRestrictions =
            LegalRestriction(
                id = LegalRestrictionsId(
                    ChannelFactory.aValidId()
                ),
                text = "New restrictions"
            )

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceLegalRestrictions(
                    channel.id,
                    legalRestrictions
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.legalRestriction).isEqualTo(legalRestrictions)
    }

    @Test
    fun `replace curriculumAligned`() {
        val channel = mongoChannelRepository.create(createChannel())
        val curriculumAligned = "this is a curriculum"

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceCurriculumAligned(
                    channel.id,
                    curriculumAligned
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.pedagogyInformation?.curriculumAligned).isEqualTo(curriculumAligned)
    }

    @Test
    fun `replace isTranscriptProvided`() {
        val channel = mongoChannelRepository.create(createChannel())
        val isTranscriptProvided = true

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceIsTranscriptProvided(
                    channel.id,
                    isTranscriptProvided
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.pedagogyInformation?.isTranscriptProvided).isEqualTo(isTranscriptProvided)
    }

    @Test
    fun `replace educational resources`() {
        val channel = mongoChannelRepository.create(createChannel())
        val educationalResources = "this is a educational resource"

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceEducationalResources(
                    channel.id,
                    educationalResources
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.pedagogyInformation?.educationalResources).isEqualTo(educationalResources)
    }

    @Test
    fun `replace best for tags`() {
        val channel = mongoChannelRepository.create(createChannel())
        val bestForTags = listOf("123", "456")

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceBestForTags(
                    channel.id,
                    bestForTags
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.pedagogyInformation?.bestForTags).isEqualTo(bestForTags)
    }

    @Test
    fun `replace subjects`() {
        val channel = mongoChannelRepository.create(createChannel())
        val subjects = listOf("subject 1", "subject 2")

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceSubjects(
                    channel.id,
                    subjects
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.pedagogyInformation?.subjects).isEqualTo(subjects)
    }

    @Test
    fun `replace ingest details`() {
        val channel = mongoChannelRepository.create(createChannel(ingest = ManualIngest))

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceIngestDetails(
                    channel.id,
                    YoutubeScrapeIngest(
                        listOf("http://youtube.com/channel")
                    )
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.ingest).isEqualTo(
            YoutubeScrapeIngest(
                listOf("http://youtube.com/channel")
            )
        )
    }

    @Test
    fun `replace delivery frequency`() {
        val channel =
            mongoChannelRepository.create(createChannel(deliveryFrequency = Period.ofMonths(1)))

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceDeliveryFrequency(
                    channel.id,
                    Period.ofYears(1)
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.deliveryFrequency).isEqualTo(Period.ofYears(1))
    }

    @Nested
    inner class OverridingDistributionMethods {
        @Test
        fun `replaces with stream`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    distributionMethods = emptySet()
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = channel.id,
                        distributionMethods = setOf(DistributionMethod.STREAM)
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(setOf(DistributionMethod.STREAM))
        }

        @Test
        fun `replaces with download`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    distributionMethods = emptySet()
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = channel.id,
                        distributionMethods = setOf(DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(setOf(DistributionMethod.DOWNLOAD))
        }

        @Test
        fun `replaces with all`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    distributionMethods = emptySet()
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = channel.id,
                        distributionMethods = setOf(DistributionMethod.STREAM, DistributionMethod.DOWNLOAD)
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.distributionMethods).isEqualTo(DistributionMethod.ALL)
        }

        @Test
        fun `replaces with empty`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    distributionMethods = emptySet()
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceDistributionMethods(
                        channelId = channel.id,
                        distributionMethods = emptySet()
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.distributionMethods).isEmpty()
        }
    }

    @Nested
    inner class UpdatingChannels {
        @Test
        fun `replaces educational resources`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    pedagogyInformation = PedagogyInformation(
                        educationalResources = "this is a resource"
                    )
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceEducationalResources(
                        channel.id, "New Resource"
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.pedagogyInformation?.educationalResources).isEqualTo("New Resource")
        }

        @Test
        fun `replaces best for tags`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    pedagogyInformation = PedagogyInformation(
                        bestForTags = listOf("123", "345")
                    )
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceBestForTags(
                        channel.id, listOf("555", "666")
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.pedagogyInformation?.bestForTags).containsExactlyInAnyOrder("555", "666")
        }

        @Test
        fun `replaces subjects`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    pedagogyInformation = PedagogyInformation(
                        subjects = listOf("subject 1", "subject 2")
                    )
                )
            )

            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceSubjects(
                        channel.id, listOf("subject 3", "subject 4")
                    )
                )
            )

            val updatedAsset = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedAsset.pedagogyInformation?.subjects).containsExactlyInAnyOrder("subject 3", "subject 4")
        }

        @Test
        fun `replaces contracts`() {
            val oldContract = ContentPartnerContractFactory.sample(contentPartnerName = "old")
            val channel = mongoChannelRepository.create(createChannel(contract = oldContract))

            val newContract = ContentPartnerContractFactory.sample(contentPartnerName = "new")
            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceContract(
                        channelId = channel.id,
                        contract = newContract
                    )
                )
            )

            val updated = mongoChannelRepository.findById(channel.id)!!
            assertThat(updated.contract).isEqualTo(newContract)
        }

        @Nested
        inner class FindByContractId {
            @Test
            fun `can find one by contract id`() {
                val contract = ContentPartnerContractFactory.sample()
                val channel = mongoChannelRepository.create(
                    createChannel(
                        contract = contract
                    )
                )

                val retrievedChannels = mongoChannelRepository.findByContractId(contractId = contract.id)

                assertThat(retrievedChannels).containsExactly(channel)
            }

            @Test
            fun `can multiple by contract id`() {
                val contract = ContentPartnerContractFactory.sample()
                val firstChannel = mongoChannelRepository.create(
                    createChannel(
                        contract = contract
                    )
                )

                val secondChannel = mongoChannelRepository.create(
                    createChannel(
                        contract = contract
                    )
                )

                mongoChannelRepository.create(
                    createChannel(
                        contract = null
                    )
                )

                val retrievedChannels = mongoChannelRepository.findByContractId(contractId = contract.id)

                assertThat(retrievedChannels).containsExactlyInAnyOrder(
                    firstChannel,
                    secondChannel
                )
            }
        }
    }
}
