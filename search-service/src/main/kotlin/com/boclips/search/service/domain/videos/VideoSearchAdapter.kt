package com.boclips.search.service.domain.videos

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery

abstract class VideoSearchAdapter<T>(
    private val indexReader: IndexReader<VideoMetadata, VideoQuery>,
    private val indexWriter: IndexWriter<VideoMetadata>
) : IndexReader<VideoMetadata, VideoQuery>,
    IndexWriter<T> {

    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.safeRebuildIndex(items.map(::convert), notifier)
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        indexWriter.upsert(items.map(::convert), notifier)
    }

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return indexReader.search(searchRequest)
    }

    override fun count(query: VideoQuery): Long {
        return indexReader.count(query)
    }

    override fun removeFromSearch(itemId: String) {
        indexWriter.removeFromSearch(itemId)
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        indexWriter.bulkRemoveFromSearch(itemIds)
    }

    override fun makeSureIndexIsThere() {
        indexWriter.makeSureIndexIsThere()
    }

    abstract fun convert(document: T): VideoMetadata
}
