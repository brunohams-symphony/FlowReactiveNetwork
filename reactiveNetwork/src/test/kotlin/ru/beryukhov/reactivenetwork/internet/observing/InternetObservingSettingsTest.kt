package ru.beryukhov.reactivenetwork.internet.observing

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings.Companion.builder
import ru.beryukhov.reactivenetwork.internet.observing.InternetObservingSettings.Companion.create
import ru.beryukhov.reactivenetwork.internet.observing.error.DefaultErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.strategy.SocketInternetObservingStrategy
import ru.beryukhov.reactivenetwork.internet.observing.strategy.WalledGardenInternetObservingStrategy

@RunWith(RobolectricTestRunner::class)
class InternetObservingSettingsTest {
    @Test
    fun shouldCreateSettings() { // when
        val settings = create()
        // then
        assertThat(settings).isNotNull()
    }

    @Test
    fun shouldBuildSettingsWithDefaultValues() { // when
        val settings = create()
        // then
        assertThat(settings.initialInterval()).isEqualTo(0)
        assertThat(settings.interval()).isEqualTo(2000)
        assertThat(settings.host()).isEqualTo("http://clients3.google.com/generate_204")
        assertThat(settings.port()).isEqualTo(80)
        assertThat(settings.timeout()).isEqualTo(2000)
        assertThat(settings.httpResponse()).isEqualTo(204)
        assertThat(settings.errorHandler())
            .isInstanceOf(DefaultErrorHandler::class.java)
        assertThat(settings.strategy())
            .isInstanceOf(WalledGardenInternetObservingStrategy::class.java)
    }

    @Test
    fun shouldBuildSettings() {
        // given
        val initialInterval = 1
        val interval = 2
        val host = "www.test.com"
        val port = 90
        val timeout = 3
        val httpResponse = 200
        val testErrorHandler =
            createTestErrorHandler()
        val strategy = SocketInternetObservingStrategy()
        // when
        val settings = builder()
            .initialInterval(initialInterval)
            .interval(interval)
            .host(host)
            .port(port)
            .timeout(timeout)
            .httpResponse(httpResponse)
            .errorHandler(testErrorHandler)
            .strategy(strategy)
            .build()
        // then
        assertThat(settings.initialInterval()).isEqualTo(initialInterval)
        assertThat(settings.interval()).isEqualTo(interval)
        assertThat(settings.host()).isEqualTo(host)
        assertThat(settings.port()).isEqualTo(port)
        assertThat(settings.timeout()).isEqualTo(timeout)
        assertThat(settings.httpResponse()).isEqualTo(httpResponse)
        assertThat(settings.errorHandler()).isNotNull()
        assertThat(settings.errorHandler())
            .isNotInstanceOf(DefaultErrorHandler::class.java)
        assertThat(settings.strategy())
            .isInstanceOf(SocketInternetObservingStrategy::class.java)
    }

    private fun createTestErrorHandler(): ErrorHandler {
        return object : ErrorHandler {
            override fun handleError(
                exception: Exception?,
                message: String?
            ) {
            }
        }
    }
}
