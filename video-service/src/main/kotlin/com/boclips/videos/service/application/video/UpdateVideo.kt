package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import mu.KLogging
import org.springframework.validation.annotation.Validated

@Validated
open class UpdateVideo(
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository
) {

    companion object : KLogging();

    open operator fun invoke(
        id: String,
        title: String?,
        description: String?,
        promoted: Boolean?,
        subjectIds: List<String>?,
        user: User
    ) {
        if (user.isPermittedToUpdateVideo.not()) throw OperationForbiddenException()

        val updateTitle = title?.let { VideoUpdateCommand.ReplaceTitle(VideoId(id), it) }
        val updateDescription = description?.let { VideoUpdateCommand.ReplaceDescription(VideoId(id), it) }
        val replacePromoted = promoted?.let { VideoUpdateCommand.ReplacePromoted(VideoId(id), it) }
        val updateSubjectIds = subjectIds?.let { subjectIdList ->
            val allSubjects = subjectRepository.findAll()
            val validNewSubjects = allSubjects.filter { subjectIdList.contains(it.id.value) }
            VideoUpdateCommand.ReplaceSubjects(VideoId(id), validNewSubjects)
        }
        val updateSubjectsWereSetManually = subjectIds?.let {
            VideoUpdateCommand.ReplaceSubjectsWereSetManually(VideoId(id), true)
        }

        videoRepository.bulkUpdate(
            listOfNotNull(
                updateTitle,
                updateDescription,
                replacePromoted,
                updateSubjectIds,
                updateSubjectsWereSetManually
            )
        )

        logger.info { "Updated video $id" }
    }
}
