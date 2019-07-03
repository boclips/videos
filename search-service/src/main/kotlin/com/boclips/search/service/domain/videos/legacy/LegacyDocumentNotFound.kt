package com.boclips.search.service.domain.videos.legacy

class LegacyDocumentNotFound(videoId: String) : Exception("Video $videoId not found in legacy index")
