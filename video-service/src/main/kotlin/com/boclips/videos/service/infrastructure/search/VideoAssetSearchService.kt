package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.SearchServiceAdapter
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.ContentFilters
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.service.SearchService

class VideoAssetSearchService(inner: GenericSearchService<VideoMetadata>) : SearchServiceAdapter<VideoAsset>(inner), SearchService {

    override fun convert(document: VideoAsset): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }

    override fun upsert(videos: Sequence<VideoAsset>) {
        super<SearchServiceAdapter>.upsert(videos.filter { ContentFilters.isInTeacherProduct(it) })
    }
}