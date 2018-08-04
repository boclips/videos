package com.boclips.videoanalyser.infrastructure.duplicates

import com.boclips.videoanalyser.domain.model.BoclipsVideo
import com.boclips.videoanalyser.domain.model.DuplicateVideo
import com.boclips.videoanalyser.domain.service.DuplicateService
import com.boclips.videoanalyser.infrastructure.BoclipsVideoRepository
import com.boclips.videoanalyser.infrastructure.duplicates.strategies.DuplicateStrategy
import org.springframework.stereotype.Service

@Service
class DelegatingDuplicateService(
        val strategies: Set<DuplicateStrategy>,
        val boclipsVideoRepository: BoclipsVideoRepository,
        val remapperService: VideoRemapperService
) : DuplicateService {
    override fun getDuplicates(): Set<DuplicateVideo> {
        val videos = boclipsVideoRepository.getAllVideos()

        val alreadyFoundDuplicates = mutableListOf<BoclipsVideo>()
        return strategies.flatMap {
            val latestDuplicates = it.findDuplicates(videos - alreadyFoundDuplicates)
            alreadyFoundDuplicates += latestDuplicates.flatMap { it.duplicates }
            latestDuplicates
        }.toSet()
    }

    override fun deleteDuplicates() = getDuplicates().let { duplicates ->
        duplicates.forEach { remapperService.remapBasketsPlaylistsAndCollections(it) }

        val allDuplicates = duplicates.flatMap { it.duplicates }.toSet()
        boclipsVideoRepository.deleteVideos(allDuplicates)
    }

}