package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.domain.service.BoclipsVideoService
import com.boclips.videoanalyser.domain.service.DuplicateService
import com.boclips.videoanalyser.domain.service.KalturaMediaService
import com.boclips.videoanalyser.domain.service.VideoAnalysisService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class VideoAdministration(
        private val videoAnalysisService: VideoAnalysisService,
        private val duplicateService: DuplicateService,
        private val kalturaMediaService: KalturaMediaService,
        private val boclipsVideoService: BoclipsVideoService
) {

    @ShellMethod("Remove all unplayable videos, aka VPCS (videos on Boclips but not playable on Kaltura)")
    fun removeUnplayableVideos() {
        say("We'll grab existing VPCS first. It'll take a while and nothing will be deleted until you confirm.")
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()
        return if (askYesNo("We found ${unplayableVideos.size} VPCS. Do you want to PERMANENTLY remove them?")) {
            say("Removing VPCS, hold on tight \uD83D\uDCA3")
            boclipsVideoService.deleteVideos(unplayableVideos)
            say("Success, Boclips is VPCS-free.")
        } else {
            say("Gotya, you keep your crap \uD83D\uDCA9")
        }
    }

    @ShellMethod("Consolidate duplicate videos")
    fun consolidateDuplicateVideos() {
        say("Searching for duplicates...")
        val duplicates = duplicateService.getDuplicates()
        say("We found ${duplicates.size} entries with duplicates.")
        return if (askYesNo("Do you want to remove ${duplicates.flatMap { it.duplicates }.size} videos and remap basket and playlist to the original version?")) {
            say("Removing duplicates, hold on tight \uD83D\uDCA3")
            duplicateService.deleteDuplicates(duplicates)
            say("Success, Boclips is duplicates-free.")
        } else {
            say("Gotya, you keep your crap \uD83D\uDCA9")
        }
    }

    @ShellMethod("Wipe all videos from Kaltura")
    fun removeAllVideosFromKaltura() {
        say("Collecting all videos...")
        val allVideos = kalturaMediaService.getReadyMediaEntries()
        say("We found ${allVideos.size} entries which are going to be deleted.")
        return if (askYesNo("Do you want to remove ${allVideos.size} videos? This cannot be undone.")) {
            say("Removing Kaltura videos, hold on tight \uD83D\uDCA3")
            val removedMediaEntries = kalturaMediaService.removeMediaEntries(allVideos)
            say("Success, you have successfully wiped ${removedMediaEntries.size} videos.")
        } else {
            say("Gotya, you keep your crap \uD83D\uDCA9")
        }
    }
}
