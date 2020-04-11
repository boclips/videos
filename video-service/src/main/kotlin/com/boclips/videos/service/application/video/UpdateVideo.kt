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
class UpdateVideo(
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository
) {
    companion object : KLogging();

    operator fun invoke(id: String, updateRequest: UpdateVideoRequest, user: User) {
        if (user.isPermittedToUpdateVideo.not()) throw OperationForbiddenException()
        val videoId = VideoId(id)

        val updateTitle = updateRequest.title
            ?.let { VideoUpdateCommand.ReplaceTitle(videoId, it) }
        val updateDescription = updateRequest.description
            ?.let { VideoUpdateCommand.ReplaceDescription(videoId, it) }
        val replacePromoted = updateRequest.promoted
            ?.let { VideoUpdateCommand.ReplacePromoted(videoId, it) }
        val updateSubjectIds = updateRequest.subjectIds
            ?.let { subjectIdList ->
                val allSubjects = subjectRepository.findAll()
                val validNewSubjects = allSubjects.filter { subjectIdList.contains(it.id.value) }
                VideoUpdateCommand.ReplaceSubjects(videoId, validNewSubjects)
            }
        val updateManuallySetSubjects = updateRequest.subjectIds
            ?.let { VideoUpdateCommand.ReplaceSubjectsWereSetManually(videoId, true) }
        val ageRange = AgeRange
            .of(min = updateRequest.ageRangeMin, max = updateRequest.ageRangeMax, curatedManually = true)
        val replaceAgeRange = VideoUpdateCommand.ReplaceAgeRange(videoId = videoId, ageRange = ageRange)
        val replaceAttachments = AttachmentRequestConverter().convert(videoId, updateRequest.attachments)

        videoRepository.bulkUpdate(
            listOfNotNull(
                updateTitle,
                updateDescription,
                replacePromoted,
                updateSubjectIds,
                updateManuallySetSubjects,
                replaceAgeRange,
                replaceAttachments
            )
        )

        logger.info { "Successfully updated video $id" }
    }
}
