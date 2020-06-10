package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartnerId

sealed class VideoFilter {
    data class ContentPartnerNameIs(val contentPartnerName: String) : VideoFilter()
    data class ContentPartnerIdIs(val contentPartnerId: ContentPartnerId) : VideoFilter()
    data class HasContentType(val type: ContentType) : VideoFilter()
    data class HasSubjectId(val subjectId: SubjectId) : VideoFilter()
    class HasVideoId(vararg val videoId: VideoId): VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
}
