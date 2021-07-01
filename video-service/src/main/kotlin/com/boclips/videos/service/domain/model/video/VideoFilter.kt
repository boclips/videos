package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.channel.ChannelId

sealed class VideoFilter {
    //no transcript
    //human-gen transcript but no topics


    //no topics AND transcript is human generated or blank
    data class IsVoicedAndMissingAnalysisData(val channelId: ChannelId) : VideoFilter()
    data class ChannelNameIs(val name: String) : VideoFilter()
    data class ChannelIdIs(val channelId: ChannelId) : VideoFilter()
    data class HasContentType(val type: VideoType) : VideoFilter()
    data class HasSubjectId(val subjectId: SubjectId) : VideoFilter()
    class HasVideoId(vararg val videoId: VideoId) : VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
    object IsDeactivated : VideoFilter()
    object IsMarkedForTranscriptGeneration : VideoFilter()
}
