package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.CreateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.YoutubeScrapeIngest
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.videos.api.response.channel.IngestDetailsResource
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChannelServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var channelService: ChannelService

    @Test
    fun `returns success when creating valid channel`() {
        val channelToBeSaved =
            ChannelFactory.createChannel(name = "hello", ingest = YoutubeScrapeIngest(playlistIds = emptyList()))

        val result = channelService.create(channelToBeSaved) as CreateChannelResult.Success
        assertThat(result.channel.name).isEqualTo(channelToBeSaved.name)
    }

    @Test
    fun `returns NameConflict when a channel already exists with the same name`() {
        val originalChannel =
            ChannelFactory.createChannel(name = "hello", ingest = YoutubeScrapeIngest(playlistIds = emptyList()))
        channelService.create(originalChannel)

        val newChannel =
            ChannelFactory.createChannel(name = "hello", ingest = YoutubeScrapeIngest(playlistIds = emptyList()))
        val result = channelService.create(newChannel) as CreateChannelResult.NameConflict
        assertThat(result.name).isEqualTo(newChannel.name)
    }

    @Test
    fun `returns conflict when a channel exists with the same hubspot id`() {
        val originalChannel = ChannelFactory.createChannel(
            name = "hello",
            ingest = YoutubeScrapeIngest(playlistIds = emptyList()),
            hubspotId = "123"
        )
        channelService.create(originalChannel)

        val newChannel = ChannelFactory.createChannel(
            name = "new",
            ingest = YoutubeScrapeIngest(playlistIds = emptyList()),
            hubspotId = "123"
        )
        val result = channelService.create(newChannel) as CreateChannelResult.HubSpotIdConflict
        assertThat(result.hubSpotId).isEqualTo("123")
    }

    @Test
    fun `returns missing contract result when channel does not have a valid contract`() {
        val newChannel = ChannelFactory.createChannel(name = "new", ingest = MrssFeedIngest(urls = emptyList()))
        val result = channelService.create(newChannel)
        assertThat(result).isInstanceOf(CreateChannelResult.MissingContract::class.java)
    }

    @Test
    fun `returns success for a valid update`() {
        val channel = saveChannel(name = "hello", ingest = IngestDetailsResource.youtube())
        val result = channelService.update(
            update = SingleChannelUpdate(
                id = channel.id,
                updateCommands = listOf(ChannelUpdateCommand.ReplaceName(channelId = channel.id, name = "new name"))
            )
        )

        result as UpdateChannelResult.Success
        assertThat(result.channel.name).isEqualTo("new name")
    }

    @Test
    fun `fails when updating a channel that does not exist`() {
        val channelId = ChannelId(TestFactories.aValidId())
        val result = channelService.update(
            update = SingleChannelUpdate(
                id = channelId,
                updateCommands = listOf(ChannelUpdateCommand.ReplaceName(channelId = channelId, name = "new name"))
            )
        )

        result as UpdateChannelResult.ChannelNotFound
        assertThat(result.channelId).isEqualTo(channelId)
    }

    @Test
    fun `fails when changing ingest type to non-YT without a contract`() {
        val newChannel =
            ChannelFactory.createChannel(name = "hello", ingest = YoutubeScrapeIngest(playlistIds = emptyList()))
        val channel = (channelService.create(newChannel) as CreateChannelResult.Success).channel

        val result = channelService.update(
            update = SingleChannelUpdate(
                id = channel.id,
                updateCommands = listOf(
                    ChannelUpdateCommand.ReplaceIngestDetails(
                        channelId = channel.id,
                        ingest = ManualIngest
                    )
                )
            )
        )


        result as UpdateChannelResult.MissingContract
        assertThat(result.channelId).isEqualTo(channel.id)
    }

    @Test
    fun `success when changing ingest type to non-yt whilst also adding a contract`() {
        val contract = saveContract()
        val newChannel =
            ChannelFactory.createChannel(name = "hello", ingest = YoutubeScrapeIngest(playlistIds = emptyList()))
        val channel = (channelService.create(newChannel) as CreateChannelResult.Success).channel

        val result = channelService.update(
            update = SingleChannelUpdate(
                id = channel.id,
                updateCommands = listOf(
                    ChannelUpdateCommand.ReplaceIngestDetails(channelId = channel.id, ingest = ManualIngest),
                    ChannelUpdateCommand.ReplaceContract(channelId = channel.id, contract = contract)
                )
            )
        )

        result as UpdateChannelResult.Success
        assertThat(result.channel.ingest).isEqualTo(ManualIngest)
        assertThat(result.channel.contract).isEqualTo(contract)
    }
}
