package com.boclips.videos.service.application.video.search

class SearchVideo(
        private val getVideoById: GetVideoById,
        private val getAllVideosById: GetAllVideosById,
        private val getVideosByQuery: GetVideosByQuery
) {

    fun byId(id: String?) = getVideoById(id)

    fun byIds(ids: List<String>) = getAllVideosById(ids)

    fun byQuery(
            query: String?,
            includeTags: List<String>,
            excludeTags: List<String>,
            pageSize: Int,
            pageNumber: Int
    ) = getVideosByQuery(query, includeTags, excludeTags, pageSize, pageNumber)
}