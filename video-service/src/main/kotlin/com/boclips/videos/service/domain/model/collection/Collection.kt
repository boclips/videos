package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.ZonedDateTime

data class Collection(
    val id: CollectionId,
    val owner: UserId,
    val title: String,
    val videos: List<VideoId>,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val discoverable: Boolean,
    val promoted: Boolean,
    val createdByBoclips: Boolean,
    val bookmarks: Set<UserId>,
    val subjects: Set<Subject>,
    val ageRange: AgeRange,
    val description: String?,
    val attachments: Set<Attachment>
) {
    fun createdBy(): String {
        return if (createdByBoclips) {
            "Boclips"
        } else {
            "Teacher"
        }
    }

    fun isOwner(owner: User) = owner.id == this.owner
    fun isBookmarkedBy(user: User) = bookmarks.contains(user.id)
}
