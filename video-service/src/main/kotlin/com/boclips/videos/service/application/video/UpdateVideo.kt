package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.service.TagRepository
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.ContentWarningRepository
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoUpdateService
import mu.KLogging
import org.springframework.validation.annotation.Validated

@Validated
class UpdateVideo(
    private val videoUpdateService: VideoUpdateService,
    private val videoRepository: VideoRepository,
    private val subjectRepository: SubjectRepository,
    private val tagRepository: TagRepository,
    private val contentWarningRepository: ContentWarningRepository
) {
    companion object : KLogging();

    operator fun invoke(id: String, updateRequest: UpdateVideoRequest, user: User) {
        if (user.isPermittedToUpdateVideo.not()) throw OperationForbiddenException()
        val videoId = VideoId(id)
        val video = videoRepository.find(videoId) ?: throw VideoNotFoundException(videoId)

        val updateTitle = updateRequest.title
            ?.let { VideoUpdateCommand.ReplaceTitle(videoId, it) }
        val updateDescription = updateRequest.description
            ?.let { VideoUpdateCommand.ReplaceDescription(videoId, it) }
        val updateAdditionalDescription = updateRequest.additionalDescription
            ?.let { VideoUpdateCommand.ReplaceAdditionalDescription(videoId, it) }
        val replacePromoted = updateRequest.promoted
            ?.let { VideoUpdateCommand.ReplacePromoted(videoId, it) }
        val updateSubjectIds = updateRequest.subjectIds
            ?.let { subjectIdList ->
                val allSubjects = subjectRepository.findAll()
                val validNewSubjects = allSubjects.filter { subjectIdList.contains(it.id.value) }
                VideoUpdateCommand.ReplaceSubjects(videoId, validNewSubjects)
            }
        val updateContentWarnings = updateRequest.contentWarningIds?.let { contentWarningIds ->
            val newContentWarnings = contentWarningRepository.findAll().filter { contentWarningIds.contains(it.id.value) }
            VideoUpdateCommand.ReplaceContentWarnings(videoId, newContentWarnings)
        }
        val updateManuallySetSubjects = updateRequest.subjectIds
            ?.let { VideoUpdateCommand.ReplaceSubjectsWereSetManually(videoId, true) }
        val ageRange = AgeRange
            .of(min = updateRequest.ageRangeMin, max = updateRequest.ageRangeMax, curatedManually = true)
        val replaceAgeRange = when {
            updateRequest.ageRangeMin != null || updateRequest.ageRangeMax!= null -> VideoUpdateCommand.ReplaceAgeRange(videoId = videoId, ageRange = ageRange)
            else -> null
        }
        val replaceAttachments = AttachmentRequestConverter().convert(videoId, updateRequest.attachments)
        val replaceBestFor = updateRequest.tagId?.let {
            tagRepository.findById(TagId(value = updateRequest.tagId!!))?.let {
                VideoUpdateCommand.ReplaceTag(videoId = videoId, tag = UserTag(tag = it, userId = user.idOrThrow()))
            }
        }

        videoUpdateService.update(
            video,
            listOfNotNull(
                updateTitle,
                updateDescription,
                updateAdditionalDescription,
                replacePromoted,
                updateSubjectIds,
                updateManuallySetSubjects,
                updateContentWarnings,
                replaceAgeRange,
                replaceAttachments,
                replaceBestFor
            )
        )

        logger.info { "Successfully updated video $id" }
    }
}
