package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.subjects.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId
import getCurrentUserId
import java.time.Instant

data class Collection(
    val id: CollectionId,
    val owner: UserId,
    val title: String,
    val videos: List<VideoId>,
    val updatedAt: Instant,
    val isPublic: Boolean,
    val createdByBoclips: Boolean,
    val bookmarks: Set<UserId>,
    val subjects: Set<SubjectId>,
    val ageRange: AgeRange
) {
    fun createdBy(): String {
        return if (createdByBoclips) {
            "Boclips"
        } else {
            "Teacher"
        }
    }

    fun isMine() = getCurrentUserId() == this.owner
    fun isBookmarked() = bookmarks.contains(getCurrentUserId())
}
