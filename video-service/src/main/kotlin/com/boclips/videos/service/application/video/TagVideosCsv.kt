package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.GetCategoryWithAncestors
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.TagRepository
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.presentation.converters.CategoryMappingMetadata
import com.boclips.videos.service.presentation.converters.VideosCategoryMetadataConverter

class TagVideosCsv(
    private val videoRepository: VideoRepository,
    private val tagRepository: TagRepository,
    private val getCategoryWithAncestors: GetCategoryWithAncestors
) {

    operator fun invoke(entries: List<CategoryMappingMetadata>, user: User) {
        withCategories(VideosCategoryMetadataConverter.convertCategories(entries))
        withPedagogyTags(VideosCategoryMetadataConverter.convertTags(entries), user)
    }

    private fun withCategories(videosToCategories: Map<VideoId, List<String>>) {
        val videoUpdateCommands = videosToCategories.map { videoToCategories ->
            VideoUpdateCommand.AddCategories(
                videoId = videoToCategories.key,
                categories = videoToCategories.value.map { getCategoryWithAncestors(it) }.toSet(),
                source = CategorySource.MANUAL
            )
        }

        videoRepository.bulkUpdate(videoUpdateCommands)
    }

    private fun withPedagogyTags(videosToTag: Map<VideoId, List<String>>, user: User) {
        val tags = tagRepository.findAll()

        val videoUpdateCommands = videosToTag.map { videoToTag ->
            when (videoToTag.value.isNotEmpty()) {
                true -> VideoUpdateCommand.UpdateTags(
                    videoId = videoToTag.key,
                    tags = videoToTag.value.map { tagLabel ->
                        UserTag(
                            tag = tags.find { tag -> tag.label == tagLabel }!!,
                            userId = user.id!!
                        )
                    }.toSet()
                )
                else -> return
            }
        }

        videoRepository.bulkUpdate(videoUpdateCommands)
    }
}
