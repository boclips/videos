package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.infrastructure.video.mongo.DistributionMethodDocument
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DistributionMethodDocumentConverterTest {
    @Test
    fun `throws when unknown delivery method is converted`() {
        assertThrows<UnknownDeliveryMethodException> {
            DistributionMethodDocumentConverter.fromDocument(
                DistributionMethodDocument(deliveryMethod = "You're nothing but a drifter who found a bag of mail.")
            )
        }
    }
}
