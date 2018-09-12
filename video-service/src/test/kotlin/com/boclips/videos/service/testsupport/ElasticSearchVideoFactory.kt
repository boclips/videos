package com.boclips.videos.service.testsupport

import com.boclips.videos.service.infrastructure.search.ElasticSearchVideo

object ElasticSearchVideoFactory {

    fun create(
            id: String = "video-id",
            referenceId: String = "ref-id",
            title: String = "video-title",
            source: String = "video-source",
            date: String = "2018-01-02",
            description: String = "video-description"
    ) = ElasticSearchVideo(
            id = id,
            referenceId = referenceId,
            title = title,
            source = source,
            date = date,
            description = description
    )

}

