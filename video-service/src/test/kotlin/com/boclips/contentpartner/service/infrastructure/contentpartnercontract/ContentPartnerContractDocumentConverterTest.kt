package com.boclips.contentpartner.service.infrastructure.contentpartnercontract

import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ContentPartnerContractDocumentConverterTest {
    @Test
    fun `converting to a document and back again matches the original contract`() {
        val converter =
            ContentPartnerContractDocumentConverter()
        val original = ContentPartnerContractFactory.sample()
        val converted = converter.toContract(converter.toDocument(original))
        assertThat(original).isEqualTo(converted)
    }
}
