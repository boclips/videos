package com.boclips.videos.service

import com.boclips.events.config.Subscriptions
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

@Component
class SwallowUnwantedPubSubMessages {

    @StreamListener(Subscriptions.VIDEO_UPDATED)
    fun ignoreVideoUpdated() {}

    @StreamListener(Subscriptions.VIDEO_ANALYSIS_REQUESTED)
    fun ignoreVideoAnalysisRequested() {}

    @StreamListener(Subscriptions.VIDEO_INDEXED)
    fun ignoreVideoIndexed() {}

    @StreamListener(Subscriptions.LEGACY_ORDER_SUBMITTED)
    fun ignoreLegacyOrderSubmitted() {}

    @StreamListener(Subscriptions.USER_ACTIVATED)
    fun ignoreUserActivated() {}

    @StreamListener(Subscriptions.VIDEOS_SEARCHED)
    fun ignoreVideosSearched() {}

    @StreamListener(Subscriptions.VIDEO_SEGMENT_PLAYED)
    fun ignoreVideoSegmentPlayed() {}

    @StreamListener(Subscriptions.VIDEO_PLAYER_INTERACTED_WITH)
    fun ignoreVideoPlayerInteractedWith() {}

    @StreamListener(Subscriptions.COLLECTION_AGE_RANGE_CHANGED)
    fun ignoreCollectionAgeRangeChanged() {}

    @StreamListener(Subscriptions.COLLECTION_BOOKMARK_CHANGED)
    fun ignoreCollectionBookmarkChanged() {}

    @StreamListener(Subscriptions.COLLECTION_VISIBILITY_CHANGED)
    fun ignoreCollectionVisibilityChanged() {}

    @StreamListener(Subscriptions.COLLECTION_RENAMED)
    fun ignoreCollectionRenamed() {}

    @StreamListener(Subscriptions.COLLECTION_SUBJECTS_CHANGED)
    fun ignoreCollectionSubjectsChanged() {}

    @StreamListener(Subscriptions.VIDEOS_INCLUSION_IN_SEARCH_REQUESTED)
    fun ignoreVideosInclusionInSearchRequested() {}

    @StreamListener(Subscriptions.VIDEOS_EXCLUSION_FROM_SEARCH_REQUESTED)
    fun ignoreVideosExclusionFromSearchRequested() {}

    @StreamListener(Subscriptions.VIDEO_REMOVED_FROM_COLLECTION)
    fun ignoreVideoRemovedFromCollection() {}

    @StreamListener(Subscriptions.VIDEO_ADDED_TO_COLLECTION)
    fun ignoreVideoAddedToCollection() {}

    @StreamListener(Subscriptions.VIDEO_SUBJECT_CLASSIFICATION_REQUESTED)
    fun ignoreVideoSubjectClassificationRequested() {}

}
