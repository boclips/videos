package com.boclips.contentpartner.service.infrastructure.contentpartner

import com.boclips.contentpartner.service.infrastructure.UnknownDeliveryMethodException
import com.boclips.contentpartner.service.infrastructure.channel.converters.DistributionMethodDocumentConverter
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
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
