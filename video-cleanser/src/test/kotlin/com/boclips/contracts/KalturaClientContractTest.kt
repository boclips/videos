package com.boclips.contracts

import com.boclips.cleanser.infrastructure.kaltura.KalturaClient
import com.boclips.cleanser.infrastructure.kaltura.KalturaProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("staging")
class KalturaClientContractTest {

    @Test
    fun canFetchMediaEntries() {
        val kalturaClient = KalturaClient(KalturaProperties(
                host = "https://www.kaltura.com",
                session = generateSession())
        )

        val mediaEntries = kalturaClient.fetch()

        assertThat(mediaEntries).isNotEmpty
    }

    private fun generateSession() =
            "djJ8MjM5NDE2Mnz1bsLCSdSyEHJ9UiodNk3V8dVev7IXy23yRUMQvet87wdx8O5c86aHyf_A1UgzSMgvJhiaYzphNgJlurpnu0mwRoT1xe5v0u2aYcx51gYxqucjVXDsGZj4bWEzHmP7wPchejojjtte67OBha2dVjLfbMrTRFywXkENS0PDWEcvCg=="
}