package com.boclips.videos.service.application.search

import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex
import mu.KLogging

class RebuildSubjectIndex(
    private val subjectRepository: SubjectRepository,
    private val subjectIndex: SubjectIndex
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")
        subjectRepository.streamAll { subjects ->
            subjectIndex.safeRebuildIndex(subjects, notifier)
        }
    }
}