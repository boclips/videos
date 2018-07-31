package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.domain.duplicates.service.DelegatingDuplicateService
import com.boclips.videoanalyser.domain.common.service.BoclipsVideoService
import com.boclips.videoanalyser.domain.analysis.service.VideoAnalysisService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class VideoAdministration(
        private val videoAnalysisService: VideoAnalysisService,
        private val duplicateService: DelegatingDuplicateService,
        private val boclipsVideoService: BoclipsVideoService
) {

    @ShellMethod("Remove all unplayable videos, aka VPCS (videos on Boclips but not playable on Kaltura)")
    fun removeUnplayableVideos() {
        say("We'll grab existing VPCS first. It'll take a while and nothing will be deleted until you confirm.")
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()
        return if(askYesNo("We found ${unplayableVideos.size} VPCS. Do you want to PERMANENTLY remove them?")) {
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
        return if(askYesNo("Do you want to remove ${duplicates.flatMap { it.duplicates }.size} videos and remap basket and playlist to the original version?")) {
            say("Removing duplicates, hold on tight \uD83D\uDCA3")
            TODO("we never implemented this shit")
            say("Success, Boclips is duplicates-free.")
        } else {
            say("Gotya, you keep your crap \uD83D\uDCA9")
        }
    }

}
