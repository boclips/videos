package com.boclips.videos.service.application.video

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
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
    private val subjectRepository: SubjectRepository,
    private val ageRangeRepository: AgeRangeRepository
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
        val updateAgeRangeIds = updateRequest.ageRangeIds?.let { idList ->
            val ranges: List<AgeRange> = idList.mapNotNull { ageRangeRepository.findById(
                AgeRangeId(
                    it
                )
            ) }
            val lowerBound = ranges.map { it.min }.min()

            val rangeMax: Int? = if (ranges.map { it.max }.any { it == null }) null else {
                ranges.mapNotNull { it.max }.max()
            }

            VideoUpdateCommand.ReplaceAgeRange(
                VideoId(id), com.boclips.videos.service.domain.model.AgeRange.bounded(
                    min = lowerBound, max = rangeMax
                )
            )
        }

        videoRepository.bulkUpdate(
            listOfNotNull(
                updateTitle,
                updateDescription,
                replacePromoted,
                updateSubjectIds,
                updateSubjectsWereSetManually,
                updateAgeRangeIds
            )
        )

        logger.info { "Updated video $id" }
    }
}
