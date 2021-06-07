package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.GetCategoryWithAncestors
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand

class TagVideosWithCategories(
    private val videoRepository: VideoRepository,
    private val getCategoryWithAncestors: GetCategoryWithAncestors
) {
    operator fun invoke(videosToCategories: Map<VideoId, List<String>>) {
        val videoUpdateCommands = videosToCategories.map { videoToCategories ->
            VideoUpdateCommand.AddCategories(
                videoId = videoToCategories.key,
                categories = videoToCategories.value.map { getCategoryWithAncestors(it) }.toSet(),
                source = CategorySource.MANUAL
            )
        }

        videoRepository.bulkUpdate(videoUpdateCommands)
    }
}
