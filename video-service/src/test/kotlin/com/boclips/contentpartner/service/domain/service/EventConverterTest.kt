package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerStatus
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType.INSTRUCTIONAL
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType.NEWS
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType.STOCK
import com.boclips.contentpartner.service.domain.model.contentpartner.CustomIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.YoutubeScrapeIngest
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory.createContentPartner
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.common.Specifiable
import com.boclips.videos.api.common.Specified
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Period
import java.util.Locale

class EventConverterTest {

    private val converter: EventConverter = EventConverter()

    @Test
    fun `converts id and name`() {
        val id = "the-id"
        val name = "CP name"

        val contentPartner = createContentPartner(
            id = ContentPartnerId(id),
            name = name
        )

        val payload = converter.toContentPartnerPayload(contentPartner)

        assertThat(payload.id.value).isEqualTo(id)
        assertThat(payload.name).isEqualTo(name)
    }

    @Test
    fun `converts top level details`() {
        val contentPartner = createContentPartner(
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
    }

    @Test
    fun `converts pedagogy details`() {
        val ageRangeBuckets = AgeRangeBuckets(
            listOf(
                AgeRange(AgeRangeId("age-range-1"), "6-10", 6, 10),
                AgeRange(AgeRangeId("age-range-2"), "12-16", 12, 16)
            )
        )
        val contentPartner = createContentPartner(
            pedagogyInformation = PedagogyInformation(
                // subjects = listOf(...), TODO refactor subjects in CPC so we have their names
                ageRangeBuckets = ageRangeBuckets,
                bestForTags = listOf("best-for-tag-1", "best-for-tag-2"),
                curriculumAligned = "my cool curriculum",
                educationalResources = "my cool educational resource",
                isTranscriptProvided = true
            )
        )

        val pedagogy = converter.toContentPartnerPayload(contentPartner).pedagogy

        assertThat(pedagogy).isNotNull
        assertThat(pedagogy.ageRange.min).isEqualTo(6)
        assertThat(pedagogy.ageRange.max).isEqualTo(16)
        assertThat(pedagogy.bestForTags).containsExactly("best-for-tag-1", "best-for-tag-2")
        assertThat(pedagogy.curriculumAligned).isEqualTo("my cool curriculum")
        assertThat(pedagogy.educationalResources).isEqualTo("my cool educational resource")
        assertThat(pedagogy.transcriptProvided).isTrue()
    }

    @Test
    fun `converts ingest details`() {
        val contentPartner = createContentPartner(
            ingest = ManualIngest,
            deliveryFrequency = Period.ofMonths(1)
        )

        val payload = converter.toContentPartnerPayload(contentPartner)

        assertThat(payload.ingest.deliveryFrequency.months).isEqualTo(1)
        assertThat(payload.ingest.type).isEqualTo("MANUAL")
    }

    @Test
    fun `convert marketing details`() {
        val contentPartner = createContentPartner(
            marketingInformation = ContentPartnerMarketingInformation(
                status = ContentPartnerStatus.PROMOTED,
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
    fun `convert contract`() {
        val originalContract = ContentPartnerContractFactory.sample(id = "blah", contentPartnerName = "BANANA")
        val convertedContract = converter.toContractPayload(originalContract)

        assertThat(convertedContract.contractId.value).isEqualTo("blah")
        assertThat(convertedContract.name).isEqualTo("BANANA")
    }
}
