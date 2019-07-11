package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class UserRatingDocumentConverterTest {

    @Test
    fun `converts a user rating to a document and back`() {
        val rating = UserRating(
            rating = 3,
            userId = UserId("sdf")
        )

        val document = UserRatingDocumentConverter.toDocument(rating)
        val restoredUserRating = UserRatingDocumentConverter.toRating(document)

        Assertions.assertThat(restoredUserRating).isEqualTo(rating)
    }

}