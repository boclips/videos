package com.boclips.videoanalyser.domain.duplicates.service

import com.boclips.videoanalyser.domain.duplicates.model.Duplicate
import com.boclips.videoanalyser.domain.common.model.BoclipsVideo
import com.boclips.videoanalyser.infrastructure.boclips.BoclipsVideoRepository
import org.springframework.stereotype.Service

@Service
class DelegatingDuplicateService(
        val strategies: Set<DuplicateStrategy>,
        val boclipsVideoRepository: BoclipsVideoRepository
) : DuplicateService {

    override fun getDuplicates(): Set<Duplicate> {
        val videos = boclipsVideoRepository.getAllVideos()

        val alreadyFoundDuplicates = mutableListOf<BoclipsVideo>()
        return strategies.flatMap {
            val latestDuplicates = it.findDuplicates(videos - alreadyFoundDuplicates)
            alreadyFoundDuplicates += latestDuplicates.flatMap { it.duplicates }
            latestDuplicates
        }.toSet()
    }

}