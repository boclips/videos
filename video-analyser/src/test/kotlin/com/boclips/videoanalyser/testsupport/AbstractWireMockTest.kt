package com.boclips.videoanalyser.testsupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.After
import org.junit.Before

abstract class AbstractWireMockTest {
    companion object {
        const val PORT = 8089
    }

    val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(PORT))

    @Before
    fun startAndResetWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
    }

    @After
    fun stopWireMock() {
        wireMockServer.stop()
    }
}