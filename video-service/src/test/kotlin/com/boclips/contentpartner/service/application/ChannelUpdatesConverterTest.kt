package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.channel.ChannelUpdatesConverter
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.channel.AgeRangeRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.LegalRestrictionsRequest
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Period

class ChannelUpdatesConverterTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var channelUpdatesConverter: ChannelUpdatesConverter

    @Autowired
    lateinit var legalRestrictionsRepository: LegalRestrictionsRepository

    lateinit var originalChannel: Channel

    @BeforeEach
    fun setUp() {
        originalChannel = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = "My content partner"
            )
        )
    }

    @Test
    fun `creates command for updating distribution methods`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                name = "Hello",
                distributionMethods = setOf(DistributionMethodResource.DOWNLOAD)
            )
        )
        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceDistributionMethods } as ChannelUpdateCommand.ReplaceDistributionMethods

        assertThat(command.distributionMethods).isEqualTo(setOf(DistributionMethod.DOWNLOAD))
    }

    @Test
    fun `creates command for updating the name`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                name = "Hello"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceName } as ChannelUpdateCommand.ReplaceName

        assertThat(command.name).isEqualTo("Hello")
        assertThat(command.channelId.value).isEqualTo(originalChannel.id.value)
    }

    @Test
    fun `creates command for updating the age range`() {
        createAgeRange(
            AgeRangeRequest(
                id = "early-years",
                label = "label",
                min = 1,
                max = 3
            )
        )

        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                ageRanges = listOf("early-years"),
                name = null
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceAgeRanges } as ChannelUpdateCommand.ReplaceAgeRanges

        assertThat(command.ageRangeBuckets.max).isEqualTo(3)
        assertThat(command.ageRangeBuckets.min).isEqualTo(1)
    }

    @Test
    fun `creates command for updating legal restrictions`() {
        val legalRestrictions = legalRestrictionsRepository.create("No restrictions")

        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                legalRestrictions = LegalRestrictionsRequest(
                    id = legalRestrictions.id.value
                )
            )
        )

        assertThat(commands).hasSize(1)

        assertThat(commands[0]).isInstanceOfSatisfying(ChannelUpdateCommand.ReplaceLegalRestrictions::class.java) { command ->
            assertThat(command.channelId).isEqualTo(originalChannel.id)
            assertThat(command.legalRestriction).isEqualTo(legalRestrictions)
        }
    }

    @Test
    fun `creates command for updating content partner types`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                contentTypes = listOf("NEWS", "STOCK", "INSTRUCTIONAL")
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceContentTypes } as ChannelUpdateCommand.ReplaceContentTypes

        assertThat(command.contentType).containsExactlyInAnyOrder("NEWS", "STOCK", "INSTRUCTIONAL")
    }

    @Test
    fun `creates command for updating content partner categories`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                contentCategories = listOf("DOCUMENTARY_SHORTS", "ANIMATION")
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceContentCategories } as ChannelUpdateCommand.ReplaceContentCategories

        assertThat(command.contentCategories).containsExactlyInAnyOrder("DOCUMENTARY_SHORTS", "ANIMATION")
    }

    @Test
    fun `creates command for updating content partner language`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                language = "spa"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceLanguage } as ChannelUpdateCommand.ReplaceLanguage

        assertThat(command.language).contains("spa")
    }

    @Test
    fun `creates command for updating content partner description`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                description = "This is a new description"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceDescription } as ChannelUpdateCommand.ReplaceDescription

        assertThat(command.description).contains("This is a new description")
    }

    @Test
    fun `creates command for updating content partner awards`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                awards = "This is a new award"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceAwards } as ChannelUpdateCommand.ReplaceAwards

        assertThat(command.awards).contains("This is a new award")
    }

    @Test
    fun `creates command for updating content partner hubspot id`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                hubspotId = "1a2s3d4f5g6h7j8k9l"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceHubspotId } as ChannelUpdateCommand.ReplaceHubspotId

        assertThat(command.hubspotId).contains("1a2s3d4f5g6h7j8k9l")
    }

    @Test
    fun `creates command for updating content partner notes`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                notes = "this is a note"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceNotes } as ChannelUpdateCommand.ReplaceNotes

        assertThat(command.notes).contains("this is a note")
    }

    @Test
    fun `creates command for updating content partner transcript value`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                isTranscriptProvided = true
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceIsTranscriptProvided } as ChannelUpdateCommand.ReplaceIsTranscriptProvided

        assertThat(command.isTranscriptProvided).isTrue()
    }

    @Test
    fun `creates command for updating content partner educational resources`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                educationalResources = "This is a resource"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceEducationalResources } as ChannelUpdateCommand.ReplaceEducationalResources

        assertThat(command.educationalResources).contains("This is a resource")
    }

    @Test
    fun `creates command for updating content partner best for tags`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                bestForTags = listOf("123", "456")
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceBestForTags } as ChannelUpdateCommand.ReplaceBestForTags

        assertThat(command.bestForTags).containsExactlyInAnyOrder("123", "456")
    }

    @Test
    fun `creates command for updating content partner curriculum`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                curriculumAligned = "curriculum"
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceCurriculumAligned } as ChannelUpdateCommand.ReplaceCurriculumAligned

        assertThat(command.curriculumAligned).contains("curriculum")
    }

    @Test
    fun `creates command for updating content partner subjects`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                subjects = listOf("subject 1", "subject 2")
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceSubjects } as ChannelUpdateCommand.ReplaceSubjects

        assertThat(command.subjects).containsExactlyInAnyOrder("subject 1", "subject 2")
    }

    @Test
    fun `creates a command for updating ingest details`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                ingest = IngestDetailsResource.mrss("https://mrss.feed")
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceIngestDetails } as ChannelUpdateCommand.ReplaceIngestDetails

        assertThat(command.ingest).isEqualTo(
            MrssFeedIngest(
                listOf("https://mrss.feed")
            )
        )
    }

    @Test
    fun `creates a command for updating delivery frequency`() {
        val commands = channelUpdatesConverter.convert(
            id = originalChannel.id,
            upsertChannelRequest = ChannelRequest(
                deliveryFrequency = Period.ofMonths(3)
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceDeliveryFrequency } as ChannelUpdateCommand.ReplaceDeliveryFrequency

        assertThat(command.deliveryFrequency).isEqualTo(Period.ofMonths(3))
    }

    @Test
    fun `creates command for updating contract`() {
        val newContract = saveContentPartnerContract(name = "new name")

        val commands = channelUpdatesConverter.convert(
            originalChannel.id, ChannelRequest(
                contractId = newContract.id.value
            )
        )

        val command =
            commands.find { it is ChannelUpdateCommand.ReplaceContract } as ChannelUpdateCommand.ReplaceContract

        assertThat(command.contract).isEqualTo(newContract)
    }
}
