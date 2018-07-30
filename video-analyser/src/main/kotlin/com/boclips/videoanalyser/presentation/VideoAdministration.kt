package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.domain.service.BoclipsVideoService
import com.boclips.videoanalyser.domain.service.VideoAnalysisService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class VideoAdministration(private val videoAnalysisService: VideoAnalysisService, private val boclipsVideoService: BoclipsVideoService) {

    @ShellMethod("Remove all unplayable videos, aka VPCS (videos on Boclips but not playable on Kaltura)")
    fun removeUnplayableVideos() {
        say("We'll grab existing VPCS first. It'll take a while. You'll be prompted before permanently removal. Go grab a coffee.")
        val unplayableVideos = videoAnalysisService.getUnplayableVideos()
        return if(askYesNo("We found ${unplayableVideos.size} VPCS. Do you want to PERMANENTLY remove them?")) {
            say("Removing VPCS, hold on tight \uD83D\uDCA3")
            boclipsVideoService.deleteVideos(unplayableVideos)
            say("Success, Boclips is VPCS-free")
        } else {
            say("Gottya, you keep your crap \uD83D\uDCA9")
        }

    }

}
