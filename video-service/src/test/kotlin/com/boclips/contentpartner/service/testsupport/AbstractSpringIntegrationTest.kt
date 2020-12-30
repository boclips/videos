package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.application.agerange.CreateAgeRange
import com.boclips.contentpartner.service.application.channel.CreateChannel
import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.contentpartner.service.application.contract.CreateContract
import com.boclips.contentpartner.service.application.exceptions.ChannelConflictException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.infrastructure.TestSignedLinkProvider
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.clients.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.request.channel.ContentCategoryRequest
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.boclips.videos.api.request.contract.ContractCostsRequest
import com.boclips.videos.api.request.contract.ContractRestrictionsRequest
import com.boclips.videos.api.request.contract.CreateContractRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
import com.boclips.videos.api.response.contract.ContractDatesResource
import com.boclips.videos.api.response.contract.ContractRoyaltySplitResource
import com.boclips.videos.service.application.collection.CreateCollection
import com.boclips.videos.service.application.subject.CreateSubject
import com.boclips.videos.service.application.video.CreateVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestMongoProcess
import com.boclips.videos.service.testsupport.UserFactory
import com.damnhandy.uri.template.UriTemplate
import com.jayway.jsonpath.JsonPath
import com.mongodb.MongoClient
import com.nhaarman.mockitokotlin2.reset
import de.flapdoodle.embed.mongo.MongodProcess
import mu.KLogging
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.Duration
import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.util.UUID

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles(
    "test",
    "fakes",
    "fakes-kaltura",
    "fakes-search",
    "fakes-youtube",
    "fakes-security",
    "fakes-signed-link",
    "fake-user-service"
)
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var legacyVideoSearchService: LegacyVideoSearchService

    @Autowired
    lateinit var videoIndex: VideoIndex

    @Autowired
    lateinit var channelIndex: ChannelIndex

    @Autowired
    lateinit var collectionIndex: CollectionIndex

    @Autowired
    lateinit var fakeSignedLinkProvider: TestSignedLinkProvider

    @Autowired
    lateinit var fakeKalturaClient: TestKalturaClient

    @Autowired
    lateinit var fakeYoutubePlaybackProvider: TestYoutubePlaybackProvider

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var createCollection: CreateCollection

    @Autowired
    lateinit var createChannel: CreateChannel

    @Autowired
    lateinit var createAgeRange: CreateAgeRange

    @Autowired
    lateinit var getChannels: GetChannels

    @Autowired
    lateinit var mongoClient: MongoClient

    @Autowired
    lateinit var fakeEventBus: SynchronousFakeEventBus

    @Autowired
    lateinit var createSubject: CreateSubject

    @Autowired
    lateinit var usersClient: UsersClientFake

    @Autowired
    lateinit var organisationsClient: OrganisationsClientFake

    @Autowired
    lateinit var createLegalRestrictions: CreateLegalRestrictions

    @Autowired
    lateinit var createContract: CreateContract

    @Autowired
    lateinit var cacheManager: CacheManager

    companion object : KLogging() {
        private var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess = TestMongoProcess.process
            }
        }
    }

    @BeforeEach
    fun resetState() {
        mongoClient.apply {
            listDatabaseNames()
                .filterNot { setOf("admin", "config").contains(it) }
                .forEach {
                    println("Dropping $it")
                    dropDatabase(it)
                }
        }

        collectionIndex.safeRebuildIndex(emptySequence())
        videoIndex.safeRebuildIndex(emptySequence())
        channelIndex.safeRebuildIndex(emptySequence())

        fakeYoutubePlaybackProvider.clear()
        fakeKalturaClient.clear()

        fakeEventBus.clearState()

        usersClient.clear()
        organisationsClient.clear()

        fakeSignedLinkProvider.clearLink()

        reset(legacyVideoSearchService)

        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }

    fun createMediaEntry(
        id: String = "1",
        duration: Duration = Duration.ofMinutes(1),
        status: MediaEntryStatus = MediaEntryStatus.READY
    ) =
        fakeKalturaClient.createMediaEntry(id, "ref-$id", duration, status)

    fun saveVideo(
            playbackId: PlaybackId = PlaybackId(
            type = KALTURA,
            value = "id-${UUID.randomUUID()}"
        ),
            title: String = "Some title!",
            description: String = "Some description!",
            date: String = "2018-01-01",
            duration: Duration = Duration.ofSeconds(120),
            contentProvider: String = "Reuters",
            contentProviderId: String? = null,
            contentProviderVideoId: String = "content-partner-video-id-${playbackId.value}",
            types: List<VideoType> = listOf(VideoType.INSTRUCTIONAL_CLIPS),
            keywords: List<String> = emptyList(),
            legalRestrictions: String = "",
            ageRangeMin: Int? = null,
            ageRangeMax: Int? = null,
            distributionMethods: Set<DistributionMethodResource> = setOf(
            DistributionMethodResource.DOWNLOAD,
            DistributionMethodResource.STREAM
        ),
            subjectIds: Set<String> = setOf()
    ): VideoId {
        val retrievedContentPartnerId = try {
            saveChannel(
                name = contentProvider,
                distributionMethods = distributionMethods
            ).id
        } catch (e: ChannelConflictException) {
            getChannels.invoke(name = contentProvider).firstOrNull()!!.id
        }

        when (playbackId.type) {
            KALTURA -> createMediaEntry(
                id = playbackId.value,
                duration = duration
            )
            YOUTUBE -> {
                fakeYoutubePlaybackProvider.addVideo(
                    playbackId.value,
                    "https://youtube.com/thumb/${playbackId.value}.png",
                    duration = duration
                )

                fakeYoutubePlaybackProvider.addMetadata(playbackId.value, "Another amazing YT Channel", "channel-1")
            }
        }

        val video = createVideo(
            CreateVideoRequest(
                providerId = contentProviderId ?: retrievedContentPartnerId.value,
                providerVideoId = contentProviderVideoId,
                title = title,
                description = description,
                releasedOn = LocalDate.parse(date),
                legalRestrictions = legalRestrictions,
                keywords = keywords,
                videoTypes = types.map { it.name },
                playbackId = playbackId.value,
                playbackProvider = playbackId.type.name,
                analyseVideo = false,
                ageRangeMin = ageRangeMin,
                ageRangeMax = ageRangeMax,
                subjects = subjectIds
            ),
            UserFactory.sample()
        )

        fakeEventBus.clearState()

        return video.videoId
    }

    fun saveChannel(
        name: String? = "TED",
        ageRanges: List<String> = emptyList(),
        distributionMethods: Set<DistributionMethodResource>? = null,
        currency: String? = null,
        description: String? = null,
        contentCategories: List<ContentCategoryRequest>? = null,
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: String? = null,
        ingest: IngestDetailsResource? = null,
        deliveryFrequency: Period? = null,
        oneLineDescription: String? = null,
        marketingInformation: MarketingInformationRequest? = null,
        isTranscriptProvided: Boolean? = null,
        educationalResources: String? = null,
        curriculumAligned: String? = null,
        bestForTags: List<String>? = null,
        subjects: List<String>? = null,
        contractId: String? = null
    ): Channel {
        val contract = contractId ?: saveContract(name = UUID.randomUUID().toString()).id.value
        val createdContentPartner = createChannel(
            VideoServiceApiFactory.createChannelRequest(
                name = name,
                ageRanges = ageRanges,
                distributionMethods = distributionMethods,
                currency = currency,
                description = description,
                contentCategories = contentCategories,
                hubspotId = hubspotId,
                awards = awards,
                notes = notes,
                language = language,
                ingest = ingest,
                deliveryFrequency = deliveryFrequency,
                oneLineDescription = oneLineDescription,
                marketingInformation = marketingInformation,
                isTranscriptProvided = isTranscriptProvided,
                educationalResources = educationalResources,
                curriculumAligned = curriculumAligned,
                bestForTags = bestForTags,
                subjects = subjects,
                contractId = contract
            )
        )

        fakeEventBus.clearState()

        return createdContentPartner
    }

    fun saveLegalRestrictions(text: String = "No restrictions."): LegalRestrictionsId {
        val createdResource = createLegalRestrictions(text = text)
        return LegalRestrictionsId(
            createdResource.id.value
        )
    }

    fun saveContract(
        name: String = "Contract name",
        contractDocument: String? = null,
        contractDateStart: String? = null,
        contractDateEnd: String? = LocalDate.of(2001, Month.AUGUST, 1).toString(),
        contractIsRolling: Boolean? = true,
        daysBeforeTerminationWarning: Int? = 100,
        yearsForMaximumLicense: Int? = 5,
        daysForSellOffPeriod: Int? = 4,
        royaltySplitDownload: Float? = 0.1F,
        royaltySplitStream: Float? = 0.9F,
        minimumPriceDescription: String? = "Price",
        remittanceCurrency: String? = "GBP",
        restrictions: ContractRestrictionsRequest? = null,
        costs: ContractCostsRequest? = null
    ): Contract = createContract(
        CreateContractRequest(
            contentPartnerName = name,
            contractDocument = contractDocument?.let { Specified(value = it) },
            contractDates = ContractDatesResource(
                start = contractDateStart,
                end = contractDateEnd
            ),
            contractIsRolling = contractIsRolling,
            daysBeforeTerminationWarning = daysBeforeTerminationWarning,
            yearsForMaximumLicense = yearsForMaximumLicense,
            daysForSellOffPeriod = daysForSellOffPeriod,
            royaltySplit = ContractRoyaltySplitResource(
                download = royaltySplitDownload,
                streaming = royaltySplitStream
            ),
            minimumPriceDescription = minimumPriceDescription,
            remittanceCurrency = remittanceCurrency,
            restrictions = restrictions,
            costs = costs
        )
    )

    fun ResultActions.andExpectApiErrorPayload(): ResultActions {
        return this.andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").exists())
    }

    fun ResultActions.andReturnLink(linkName: String): UriTemplate {
        val hrefPath = "$._links.$linkName.href"
        andExpect(jsonPath(hrefPath).exists())

        val response = andReturn().response.contentAsString
        val link = JsonPath.parse(response).read<String>(hrefPath)
        return UriTemplate.fromTemplate(link)
    }
}
