package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.SearchServiceAdapter
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.service.SearchService

class VideoAssetSearchService(
        genericSearchService: GenericSearchService,
        genericSearchServiceAdmin: GenericSearchServiceAdmin<VideoMetadata>)
    : SearchServiceAdapter<VideoAsset>(genericSearchService, genericSearchServiceAdmin), SearchService {

    override fun convert(document: VideoAsset): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }
}