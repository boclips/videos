package com.boclips.videos.service.application.video.search

class SearchVideo(
        private val getVideoById: GetVideoById,
        private val getAllVideosById: GetAllVideosById,
        private val getVideosByQuery: GetVideosByQuery
) {

    fun byId(id: String?) = getVideoById.execute(id)

    fun byIds(ids: List<String>) = getAllVideosById.execute(ids)

    fun byQuery(
            query: String?,
            includeTags: List<String>,
            excludeTags: List<String>,
            pageSize: Int,
            pageNumber: Int
    ) = getVideosByQuery.execute(query, includeTags, excludeTags, pageSize, pageNumber)
}