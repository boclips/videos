package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.user.User
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

    open operator fun invoke(id: String, updateRequest: UpdateVideoRequest, user: User) {
        if (user.isPermittedToUpdateVideo.not()) throw OperationForbiddenException()

        val updateTitle = updateRequest.title?.let { VideoUpdateCommand.ReplaceTitle(VideoId(id), it) }
        val updateDescription =
            updateRequest.description?.let { VideoUpdateCommand.ReplaceDescription(VideoId(id), it) }
        val replacePromoted = updateRequest.promoted?.let { VideoUpdateCommand.ReplacePromoted(VideoId(id), it) }
        val updateSubjectIds = updateRequest.subjectIds?.let { subjectIdList ->
            val allSubjects = subjectRepository.findAll()
            val validNewSubjects = allSubjects.filter { subjectIdList.contains(it.id.value) }
            VideoUpdateCommand.ReplaceSubjects(VideoId(id), validNewSubjects)
        }
        val updateSubjectsWereSetManually = updateRequest.subjectIds?.let {
            VideoUpdateCommand.ReplaceSubjectsWereSetManually(VideoId(id), true)
        }

        val ageRange = AgeRange.of(min = updateRequest.ageRangeMin, max = updateRequest.ageRangeMax)
        val replaceAgeRange = VideoUpdateCommand.ReplaceAgeRange(videoId = VideoId(id), ageRange = ageRange)

        videoRepository.bulkUpdate(
            listOfNotNull(
                updateTitle,
                updateDescription,
                replacePromoted,
                updateSubjectIds,
                updateSubjectsWereSetManually,
                replaceAgeRange
            )
        )

        logger.info { "Updated video $id" }
    }
}
