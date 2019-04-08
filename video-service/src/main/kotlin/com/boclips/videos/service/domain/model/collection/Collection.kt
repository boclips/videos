package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.asset.AssetId
import getCurrentUserId
import java.time.Instant

data class Collection(
    val id: CollectionId,
    val owner: UserId,
    val title: String,
    val videos: List<AssetId>,
    val updatedAt: Instant,
    val isPublic: Boolean,
    val createdByBoclips: Boolean,
    val bookmarks: Set<UserId>
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
