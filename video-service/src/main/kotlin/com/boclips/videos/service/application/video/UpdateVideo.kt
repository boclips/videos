package com.boclips.videos.service.application.video

import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.UserRating
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

        val updateTitle = updateRequest.title?.let { VideoUpdateCommand.ReplaceTitle(VideoId(id), it) }
        val updateDescription =
            updateRequest.description?.let { VideoUpdateCommand.ReplaceDescription(VideoId(id), it) }
        val replacePromoted = updateRequest.promoted?.let { VideoUpdateCommand.ReplacePromoted(VideoId(id), it) }
        val updateSubjectIds = updateRequest.subjectIds?.split(",")?.let { subjectIdList ->
            val allSubjects = subjectRepository.findAll()
            val validNewSubjects = allSubjects.filter { subjectIdList.contains(it.id.value) }
            VideoUpdateCommand.ReplaceSubjects(VideoId(id), validNewSubjects)
        }
        val updateSubjectsWereSetManually = updateRequest.subjectIds?.let {
            VideoUpdateCommand.ReplaceSubjectsWereSetManually(VideoId(id), true)
        }
        val ageRange =
            AgeRange.of(min = updateRequest.ageRangeMin, max = updateRequest.ageRangeMax, curatedManually = true)
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

        updateRequest.rating?.let {
            if (!user.isPermittedToRateVideos) throw OperationForbiddenException()
            videoRepository.update(
                VideoUpdateCommand.AddRating(
                    VideoId(id),
                    UserRating(rating = it, userId = user.id)
                )
            )
        }

        logger.info { "Successfully updated video $id" }
    }
}
