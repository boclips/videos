package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class MongoChannelRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoChannelRepository: ChannelRepository

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
    fun `stream all`() {
        mongoChannelRepository.create(
            createChannel(name = "good day")
        )

        mongoChannelRepository.create(
            createChannel(name = "good great day")
        )

        var channels: List<ChannelSuggestion> = emptyList()

        mongoChannelRepository.streamAll { channels = it.toList() }

        assertThat(channels).hasSize(2)
    }

    @Test
    fun `find all with multiple filters`() {
        val toBeFoundChannelId = mongoChannelRepository.create(
            createChannel(hubspotId = "123", name = "hello")
        ).id

        mongoChannelRepository.create(
            createChannel(hubspotId = "456", name = "shwmae")
        ).id

        mongoChannelRepository.create(
            createChannel(name = "hello")
        ).id

        val retrievedChannels =
            mongoChannelRepository.findAll(
                listOf(
                    ChannelFilter.HubspotIdFilter(hubspotId = "123"),
                    ChannelFilter.NameFilter(name = "hello")
                )
            )

        assertThat(retrievedChannels.map { it.id }).containsExactly(toBeFoundChannelId)
    }

    @Test
    fun `find all channel`() {
        mongoChannelRepository.create(
            createChannel(name = "my bloody valentine")
        )

        val retrievedAsset = mongoChannelRepository.findAll()

        assertThat(retrievedAsset).hasSize(1)
        assertThat(retrievedAsset.first().name).isEqualTo("my bloody valentine")
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
    fun `find all distinct channel by ids`() {
        val channel1 = mongoChannelRepository.create(createChannel(name = "TED"))
        val channel2 = mongoChannelRepository.create(createChannel(name = "TED 2"))

        val results = mongoChannelRepository.findAllByIds(listOf(channel1.id, channel2.id, channel1.id)).toList()

        assertThat(results).hasSize(2)
        assertThat(results[0].name).isEqualTo("TED")
        assertThat(results[1].name).isEqualTo("TED 2")
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

    @Test
    fun `replace categories`() {
        val channel =
            mongoChannelRepository.create(createChannel(categories = null))

        mongoChannelRepository.update(
            listOf(
                ChannelUpdateCommand.ReplaceCategories(
                    channel.id,
                    setOf(
                        CategoryWithAncestors(
                            codeValue = CategoryCode("ABC"),
                            description = "what a wonderful description",
                            ancestors = setOf(CategoryCode("A"))
                        )
                    )
                )
            )
        )

        val updatedChannel = mongoChannelRepository.findById(channel.id)
        assertThat(updatedChannel?.categories!!.first().codeValue).isEqualTo(CategoryCode("ABC"))
        assertThat(updatedChannel.categories!!.first().description).isEqualTo("what a wonderful description")
        assertThat(updatedChannel.categories!!.first().ancestors).isEqualTo(setOf(CategoryCode("A")))
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
        fun `replaces category`() {
            val channel = mongoChannelRepository.create(
                createChannel(
                    categories = emptyList()
                )
            )
            mongoChannelRepository.update(
                listOf(
                    ChannelUpdateCommand.ReplaceCategories(
                        channelId = channel.id,
                        categories = setOf(
                            CategoryWithAncestors(codeValue = CategoryCode("A"), description = "Law", ancestors = emptySet()),
                            CategoryWithAncestors(codeValue = CategoryCode("BC"), description = "Interior Design", ancestors = emptySet())
                        )
                    )
                )
            )

            val updatedChannel = mongoChannelRepository.findById(channel.id)!!
            assertThat(updatedChannel.categories).containsExactlyInAnyOrder(
                CategoryWithAncestors(codeValue = CategoryCode("A"), description = "Law"),
                CategoryWithAncestors(codeValue = CategoryCode("BC"), description = "Interior Design")
            )
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
