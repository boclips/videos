package com.boclips.search.service.domain.legacy

class SolrDocumentNotFound(videoId: String) : Exception("Video $videoId not found")
