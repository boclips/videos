package com.boclips.cleanser.infrastructure.kaltura.client

import com.boclips.cleanser.infrastructure.kaltura.KalturaProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KalturaMediaClientStagingE2E {

    @Test
    fun canFetchMediaEntries() {
        val kalturaClient = KalturaMediaClient(KalturaProperties(
                host = "https://www.kaltura.com",
                session = generateSession())
        )

        val mediaEntries = kalturaClient.fetch(10, 0)

        assertThat(mediaEntries).hasSize(10)
    }

    private fun generateSession() =
            "djJ8MjM5NDE2Mnz1bsLCSdSyEHJ9UiodNk3V8dVev7IXy23yRUMQvet87wdx8O5c86aHyf_A1UgzSMgvJhiaYzphNgJlurpnu0mwRoT1xe5v0u2aYcx51gYxqucjVXDsGZj4bWEzHmP7wPchejojjtte67OBha2dVjLfbMrTRFywXkENS0PDWEcvCg=="
}