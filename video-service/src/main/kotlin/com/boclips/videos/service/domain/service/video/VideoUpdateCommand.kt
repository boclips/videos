package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.UserRating
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.channel.Channel
import java.time.Duration
import java.util.Locale

sealed class VideoUpdateCommand(val videoId: VideoId) {
    class ReplaceAttachments(videoId: VideoId, val attachments: List<Attachment>) : VideoUpdateCommand(videoId)
    class RemoveAttachments(videoId: VideoId) : VideoUpdateCommand(videoId)
    class ReplaceSubjects(videoId: VideoId, val subjects: List<Subject>) : VideoUpdateCommand(videoId)
    class RemoveSubject(videoId: VideoId, val subjectId: SubjectId) : VideoUpdateCommand(videoId)
    class ReplaceDuration(videoId: VideoId, val duration: Duration) : VideoUpdateCommand(videoId)
    class ReplacePlayback(videoId: VideoId, val playback: VideoPlayback) : VideoUpdateCommand(videoId)
    class ReplaceThumbnailSecond(videoId: VideoId, val thumbnailSecond: Int?) : VideoUpdateCommand(videoId)
    class ReplaceCustomThumbnail(videoId: VideoId, val customThumbnail: Boolean?) : VideoUpdateCommand(videoId)
    class ReplaceLanguage(videoId: VideoId, val language: Locale) : VideoUpdateCommand(videoId)
    class ReplaceTranscript(videoId: VideoId, val transcript: String, val isHumanGenerated: Boolean?) : VideoUpdateCommand(videoId)
    class ReplaceTopics(videoId: VideoId, val eventBus: Set<Topic>) : VideoUpdateCommand(videoId)
    class ReplaceKeywords(videoId: VideoId, val keywords: Set<String>) : VideoUpdateCommand(videoId)
    class ReplaceAgeRange(videoId: VideoId, val ageRange: AgeRange) : VideoUpdateCommand(videoId)
    class ReplaceChannel(videoId: VideoId, val channel: Channel) : VideoUpdateCommand(videoId)
    class AddRating(videoId: VideoId, val rating: UserRating) : VideoUpdateCommand(videoId)
    class ReplaceTag(videoId: VideoId, val tag: UserTag) : VideoUpdateCommand(videoId)
    class ReplaceTitle(videoId: VideoId, val title: String) : VideoUpdateCommand(videoId)
    class ReplaceDescription(videoId: VideoId, val description: String) : VideoUpdateCommand(videoId)
    class ReplaceAdditionalDescription(videoId: VideoId, val additionalDescription: String) : VideoUpdateCommand(videoId)
    class ReplaceLegalRestrictions(videoId: VideoId, val text: String) : VideoUpdateCommand(videoId)
    class ReplacePromoted(videoId: VideoId, val promoted: Boolean) : VideoUpdateCommand(videoId)
    class ReplaceContentTypes(videoId: VideoId, val types: List<VideoType>) : VideoUpdateCommand(videoId)
    class ReplaceSubjectsWereSetManually(videoId: VideoId, val subjectsWereSetManually: Boolean) :
        VideoUpdateCommand(videoId)

    class ReplaceContentWarnings(videoId: VideoId, val contentWarnings: List<ContentWarning>) :
        VideoUpdateCommand(videoId)
    class MarkAsDuplicate(videoId: VideoId, val activeVideoId: VideoId) : VideoUpdateCommand(videoId)
    class ReplaceCategories(videoId: VideoId, val categories: Set<CategoryWithAncestors>, val source: CategorySource) : VideoUpdateCommand(videoId)
    class AddCategories(videoId: VideoId, val categories: Set<CategoryWithAncestors>, val source: CategorySource) : VideoUpdateCommand(videoId)
    class ReplaceTranscriptRequested(videoId: VideoId, val isTranscriptRequested: Boolean) : VideoUpdateCommand(videoId)
}
