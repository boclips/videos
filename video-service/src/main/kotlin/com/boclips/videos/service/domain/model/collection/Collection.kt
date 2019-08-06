package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.Instant

data class Collection(
    val id: CollectionId,
    val owner: UserId,
    val viewerIds: List<UserId>,
    val title: String,
    val videos: List<VideoId>,
    val updatedAt: Instant,
    val isPublic: Boolean,
    val createdByBoclips: Boolean,
    val bookmarks: Set<UserId>,
    val subjects: Set<Subject>,
    val ageRange: AgeRange,
    val description: String?
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
