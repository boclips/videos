package com.boclips.mysql2es

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("staging")
class PopulateStaging {

    @Autowired
    lateinit var migrationService: MigrationService

    @Test
    fun addAllVideos() {
        migrationService.migrateData("select * from metadata_orig", "videos")
    }

    @Test
    fun addCuratedVideos() {
        val curatedVideosQuery = "select * from metadata_orig where  +\n" +
                "source = '1 Minute in the Museum' or\n" +
                "source = '3Blue1Brown' or\n" +
                "source = '7 Dimensions' or\n" +
                "source = 'Advocate Of Wordz' or\n" +
                "source = 'Amor Sciendi' or\n" +
                "source = 'Ancient Lights Media' or\n" +
                "source = 'Barcroft' or\n" +
                "source = 'Basho and Friends' or\n" +
                "source = 'Bloomberg' or\n" +
                "source = 'Bozeman Science' or\n" +
                "source = 'Bridgeman' or\n" +
                "source = 'British Movietone' or\n" +
                "source = 'Britlish' or\n" +
                "source = 'Bulbul' or\n" +
                "source = 'Crash Course' or\n" +
                "source = 'Grinberg, Paramount, Pathe Newsreels' or\n" +
                "source = 'Hearst Newsreel' or\n" +
                "source = 'Hip Hughes History' or\n" +
                "source = 'iesha Learning' or\n" +
                "source = 'Immediacy Learning' or\n" +
                "source = 'Intelecom Learning' or\n" +
                "source = 'Let\\'s Tute' or\n" +
                "source = 'Maddie Moate' or\n" +
                "source = 'Math Fortress' or\n" +
                "source = 'Mazzarella Media' or\n" +
                "source = 'MCA Collection' or\n" +
                "source = 'Minute Earth' or\n" +
                "source = 'Minute Physics' or\n" +
                "source = 'Neuro Transmissions' or\n" +
                "source = 'Next Animation Studio' or\n" +
                "source = 'OOOM' or\n" +
                "source = 'PBS NewsHour' or\n" +
                "source = 'Rachel\\'s English' or\n" +
                "source = 'Science360' or\n" +
                "source = 'SciShow' or\n" +
                "source = 'SciShow Kids' or\n" +
                "source = 'SciShow Psych' or\n" +
                "source = 'SciShow Space' or\n" +
                "source = 'ShortCutsTV' or\n" +
                "source = 'Simple History' or\n" +
                "source = 'Soliloquy' or\n" +
                "source = 'Sustainable Business Consulting' or\n" +
                "source = 'TED Talks' or\n" +
                "source = 'TED-Ed' or\n" +
                "source = 'The March of Time' or\n" +
                "source = 'The School of Life' or\n" +
                "source = 'Weatherthings' or\n" +
                "source = 'XKA Digital'"

        migrationService.migrateData(curatedVideosQuery, "curated-videos")
    }

}
