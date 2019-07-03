package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.infrastructure.video.mongo.DeliveryMethodDocument
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeliveryMethodDocumentConverterTest {
    @Test
    fun `throws when unknown delivery method is converted`() {
        assertThrows<UnknownDeliveryMethod> {
            DeliveryMethodDocumentConverter.fromDocument(
                DeliveryMethodDocument(deliveryMethod = "You're nothing but a drifter who found a bag of mail.")
            )
        }
    }
}
