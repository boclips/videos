package com.boclips.videos.service.domain.model.video

data class SearchResultsWithCursor(val videos: List<Video>, val cursorId: String?)
