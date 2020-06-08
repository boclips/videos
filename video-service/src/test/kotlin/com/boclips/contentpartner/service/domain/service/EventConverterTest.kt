package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelStatus
import com.boclips.contentpartner.service.domain.model.channel.ContentType.INSTRUCTIONAL
import com.boclips.contentpartner.service.domain.model.channel.ContentType.NEWS
import com.boclips.contentpartner.service.domain.model.channel.ContentType.STOCK
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contract.ContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.contentpartner.service.testsupport.ChannelFactory.createChannel
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import com.boclips.videos.service.testsupport.SubjectFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDate
import java.time.Period
import java.util.Currency
import java.util.Locale

class EventConverterTest {

    private val converter: EventConverter = EventConverter()

    @Test
    fun `converts id and name`() {
        val id = "the-id"
        val name = "CP name"

        val contentPartner = createChannel(
            id = ChannelId(id),
            name = name
        )

        val payload = converter.toContentPartnerPayload(contentPartner)

        assertThat(payload.id.value).isEqualTo(id)
        assertThat(payload.name).isEqualTo(name)
    }

    @Test
    fun `converts top level details`() {
        val contentPartner = createChannel(
            contentTypes = listOf(
                INSTRUCTIONAL, STOCK, NEWS
            ),
            contentCategories = listOf("Animation"),
            language = Locale.CANADA_FRENCH,
            hubspotId = "hubspot-id",
            contract = ContentPartnerContractFactory.sample("contract-id"),
            awards = "Outsell Company of the Year",
            notes = "no notes yet"
        )

        val details = converter.toContentPartnerPayload(contentPartner).details

        assertThat(details).isNotNull
        assertThat(details.contentTypes).containsExactly("INSTRUCTIONAL", "STOCK", "NEWS")
        assertThat(details.contentCategories).containsExactly("Animation")
        assertThat(details.hubspotId).isEqualTo("hubspot-id")
        assertThat(details.contractId).isEqualTo("contract-id")
        assertThat(details.awards).isEqualTo("Outsell Company of the Year")
        assertThat(details.notes).isEqualTo("no notes yet")
        assertThat(details.language).isEqualTo(Locale.CANADA_FRENCH)
    }

    @Test
    fun `converts pedagogy details`() {
        val ageRangeBuckets = AgeRangeBuckets(
            listOf(
                AgeRange(AgeRangeId("age-range-1"), "6-10", 6, 10),
                AgeRange(AgeRangeId("age-range-2"), "12-16", 12, 16)
            )
        )
        val contentPartner = createChannel(
            pedagogyInformation = PedagogyInformation(
                subjects = listOf("subject-1", "subject-2"),
                ageRangeBuckets = ageRangeBuckets,
                bestForTags = listOf("best-for-tag-1", "best-for-tag-2"),
                curriculumAligned = "my cool curriculum",
                educationalResources = "my cool educational resource",
                isTranscriptProvided = true
            )
        )

        val pedagogy = converter.toContentPartnerPayload(
            channel = contentPartner,
            allSubjects = listOf(
                SubjectFactory.sample(id = "subject-1"),
                SubjectFactory.sample(id = "subject-2"),
                SubjectFactory.sample(id = "subject-3")
            )
        ).pedagogy

        assertThat(pedagogy).isNotNull
        assertThat(pedagogy.ageRange.min).isEqualTo(6)
        assertThat(pedagogy.ageRange.max).isEqualTo(16)
        assertThat(pedagogy.bestForTags).containsExactly("best-for-tag-1", "best-for-tag-2")
        assertThat(pedagogy.curriculumAligned).isEqualTo("my cool curriculum")
        assertThat(pedagogy.educationalResources).isEqualTo("my cool educational resource")
        assertThat(pedagogy.transcriptProvided).isTrue()
        assertThat(pedagogy.subjects.map { it.id.value }).containsExactly("subject-1", "subject-2")
    }

    @Test
    fun `converts ingest details`() {
        val contentPartner = createChannel(
            ingest = ManualIngest,
            deliveryFrequency = Period.ofMonths(1)
        )

        val payload = converter.toContentPartnerPayload(contentPartner)

        assertThat(payload.ingest.deliveryFrequency.months).isEqualTo(1)
        assertThat(payload.ingest.type).isEqualTo("MANUAL")
    }

    @Test
    fun `convert marketing details`() {
        val contentPartner = createChannel(
            marketingInformation = MarketingInformation(
                status = ChannelStatus.PROMOTED,
                oneLineDescription = "What a great content partner",
                logos = listOf(URL("https://cp.com/logo.jpg")),
                showreel = URL("https://google.com/"),
                sampleVideos = listOf(URL("http://hi.com"), URL("http://bye.com"))
            )
        )

        val payload = converter.toContentPartnerPayload(contentPartner).marketing
        assertThat(payload).isNotNull
        assertThat(payload.status).isEqualTo("PROMOTED")
        assertThat(payload.oneLineIntro).isEqualTo("What a great content partner")
        assertThat(payload.logos).containsExactly("https://cp.com/logo.jpg")
        assertThat(payload.showreel).isEqualTo("https://google.com/")
        assertThat(payload.sampleVideos).containsExactly("http://hi.com", "http://bye.com")
    }

    @Test
    fun `convert mostly-null contract`() {
        val originalContract = ContentPartnerContractFactory.sample(
            id = "blah",
            contentPartnerName = "BANANA",
            contractDocument = null,
            contractDates = null,
            contractIsRolling = null,
            daysBeforeTerminationWarning = null,
            yearsForMaximumLicense = null,
            daysForSellOffPeriod = null,
            royaltySplit = null,
            minimumPriceDescription = null,
            remittanceCurrency = null,
            restrictions = null,
            costs = ContractCosts(
                minimumGuarantee = emptyList(),
                upfrontLicense = null,
                technicalFee = null,
                recoupable = null
            )
        )
        val convertedContract = converter.toContractPayload(originalContract)

        assertThat(convertedContract.contractId.value).isEqualTo("blah")
        assertThat(convertedContract.name).isEqualTo("BANANA")
        assertNull(convertedContract.contractDocument)
        assertNull(convertedContract.contractDates)
        assertNull(convertedContract.contractIsRolling)
        assertNull(convertedContract.daysBeforeTerminationWarning)
        assertNull(convertedContract.yearsForMaximumLicense)
        assertNull(convertedContract.daysForSellOffPeriod)
        assertNull(convertedContract.royaltySplit)
        assertNull(convertedContract.minimumPriceDescription)
        assertNull(convertedContract.remittanceCurrency)
        assertNull(convertedContract.restrictions)

        val costs = convertedContract.costs

        assertThat(costs.minimumGuarantee).isEmpty()
        assertNull(costs.upfrontLicense)
        assertNull(costs.technicalFee)
        assertNull(costs.recoupable)
    }

    @Test
    fun `convert completely-filled-out contract`() {
        val originalContract = ContentPartnerContractFactory.sample(
            id = "contract-id",
            contentPartnerName = "contract name",
            contractDocument = "http://google.com",
            contractDates = ContractDates(
                start = LocalDate.ofYearDay(2013, 12),
                end = LocalDate.ofYearDay(2016, 300)
            ),
            contractIsRolling = true,
            daysBeforeTerminationWarning = 30,
            yearsForMaximumLicense = 3,
            daysForSellOffPeriod = 55,
            royaltySplit = ContractRoyaltySplit(
                download = 25F,
                streaming = 92.1F
            ),
            minimumPriceDescription = "min price",
            remittanceCurrency = "CAD",
            restrictions = ContractRestrictions(
                clientFacing = listOf("client-facing"),
                territory = "territory",
                editing = "editing",
                licensing = "licensing",
                marketing = "marketing",
                companies = "companies",
                payout = "payout",
                other = "other"
            ),
            costs = ContractCosts(
                minimumGuarantee = listOf(BigDecimal.TEN),
                upfrontLicense = BigDecimal.ONE,
                technicalFee = BigDecimal.ONE,
                recoupable = true
            )
        )
        val convertedContract = converter.toContractPayload(originalContract)

        assertThat(convertedContract.contractId.value).isEqualTo("contract-id")
        assertThat(convertedContract.name).isEqualTo("contract name")
        assertThat(convertedContract.contractDocument).isEqualTo("http://google.com")
        assertThat(convertedContract.contractDates.start).isEqualTo(LocalDate.ofYearDay(2013, 12))
        assertThat(convertedContract.contractDates.end).isEqualTo(LocalDate.ofYearDay(2016, 300))
        assertThat(convertedContract.contractIsRolling).isTrue()
        assertThat(convertedContract.daysBeforeTerminationWarning).isEqualTo(30)
        assertThat(convertedContract.yearsForMaximumLicense).isEqualTo(3)
        assertThat(convertedContract.daysForSellOffPeriod).isEqualTo(55)
        assertThat(convertedContract.royaltySplit.download).isEqualTo(25F)
        assertThat(convertedContract.royaltySplit.streaming).isEqualTo(92.1F)
        assertThat(convertedContract.minimumPriceDescription).isEqualTo("min price")
        assertThat(convertedContract.remittanceCurrency).isEqualTo(Currency.getInstance("CAD"))

        val restrictions = convertedContract.restrictions

        assertThat(restrictions.clientFacing).containsExactly("client-facing")
        assertThat(restrictions.territory).isEqualTo("territory")
        assertThat(restrictions.editing).isEqualTo("editing")
        assertThat(restrictions.licensing).isEqualTo("licensing")
        assertThat(restrictions.marketing).isEqualTo("marketing")
        assertThat(restrictions.companies).isEqualTo("companies")
        assertThat(restrictions.payout).isEqualTo("payout")
        assertThat(restrictions.other).isEqualTo("other")

        val costs = convertedContract.costs

        assertThat(costs.minimumGuarantee).containsExactly(BigDecimal.TEN)
        assertThat(costs.upfrontLicense).isEqualTo(BigDecimal.ONE)
        assertThat(costs.technicalFee).isEqualTo(BigDecimal.ONE)
        assertThat(costs.recoupable).isTrue()
    }
}
