package com.boclips.contentpartner.service.infrastructure.contract

import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContractDocumentConverterTest {
    @Test
    fun `converting to a document and back again matches the original contract`() {
        val converter =
            ContractDocumentConverter()
        val original = ContentPartnerContractFactory.sample()
        val converted = converter.toContract(converter.toDocument(original))
        assertThat(original).isEqualTo(converted)
    }
}
