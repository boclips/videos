package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.infrastructure.video.UserRatingDocument

object UserRatingDocumentConverter {

    fun toDocument(rating: UserRating): UserRatingDocument {
        return rating.let {
            UserRatingDocument(
                rating = it.rating,
                userId = it.userId.value
            )
        }
    }

    fun toRating(userRatingDocument: UserRatingDocument): UserRating {
        return userRatingDocument.let {
            UserRating(
                rating = it.rating,
                userId = UserId(it.userId)
            )
        }
    }
}
