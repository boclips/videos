package com.boclips.search.service.domain.legacy

import java.lang.Exception

class SolrDocumentNotFound(videoId: String) : Exception("Video $videoId not found")
