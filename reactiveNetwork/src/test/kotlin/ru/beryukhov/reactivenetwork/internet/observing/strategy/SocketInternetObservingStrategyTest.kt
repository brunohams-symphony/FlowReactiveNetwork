package ru.beryukhov.reactivenetwork.internet.observing.strategy

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@RunWith(RobolectricTestRunner::class)
class SocketInternetObservingStrategyTest {

    private val strategy = spyk(SocketInternetObservingStrategy())
    private val errorHandler = mockk<ErrorHandler>(relaxed = true)
    private val socket = mockk<Socket>(relaxed = true)

    private val host: String = strategy.getDefaultPingHost()

    @Test
    fun shouldBeConnectedToTheInternet() = runTest {
        // given
        every {
            strategy.isConnected(
                host = host,
                port = PORT,
                timeoutInMs = TIMEOUT_IN_MS,
                errorHandler = errorHandler
            )
        } returns true

        // when

        strategy.observeInternetConnectivity(
            initialIntervalInMs = INITIAL_INTERVAL_IN_MS,
            intervalInMs = INTERVAL_IN_MS,
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            httpResponse = HTTP_RESPONSE,
            errorHandler = errorHandler
        ).test {
            // then
            assertThat(awaitItem()).isEqualTo(true)
        }
    }

    @Test
    fun shouldNotBeConnectedToTheInternet() = runTest {
        // given
        every {
            strategy.isConnected(
                host = host,
                port = PORT,
                timeoutInMs = TIMEOUT_IN_MS,
                errorHandler = errorHandler
            )
        } returns false
        // when

        strategy.observeInternetConnectivity(
            initialIntervalInMs = INITIAL_INTERVAL_IN_MS,
            intervalInMs = INTERVAL_IN_MS,
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            httpResponse = HTTP_RESPONSE,
            errorHandler = errorHandler
        ).test {
            // then
            assertThat(awaitItem()).isEqualTo(false)
        }
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotBeConnectedToTheInternetWhenSocketThrowsAnExceptionOnConnect() {
        // given
        val address = InetSocketAddress(
            host,
            PORT
        )
        every { socket.connect(address, TIMEOUT_IN_MS) } throws IOException()

        // when
        val isConnected = strategy.isConnected(
            socket = socket,
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            errorHandler = errorHandler
        )
        // then
        assertThat(isConnected).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionThrownDuringClosingTheSocket() {
        // given
        val errorMsg = "Could not close the socket"
        val givenException = IOException(errorMsg)
        every { socket.close() } throws givenException

        // when
        strategy.isConnected(
            socket = socket,
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            errorHandler = errorHandler
        )
        // then
        verify(exactly = 1) { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    fun shouldBeConnectedToTheInternetViaSingle() = runTest {
        // given
        every {
            strategy.isConnected(
                host = host,
                port = PORT,
                timeoutInMs = TIMEOUT_IN_MS,
                errorHandler = errorHandler
            )
        } returns true
        // when
        val isConnected = strategy.checkInternetConnectivity(
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            httpResponse = HTTP_RESPONSE,
            errorHandler = errorHandler
        )
        // then
        assertThat(isConnected).isTrue()
    }

    @Test
    fun shouldNotBeConnectedToTheInternetViaSingle() = runTest {
        // given
        every {
            strategy.isConnected(
                host = host,
                port = PORT,
                timeoutInMs = TIMEOUT_IN_MS,
                errorHandler = errorHandler
            )
        } returns false
        // when
        val isConnected = strategy.checkInternetConnectivity(
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            httpResponse = HTTP_RESPONSE,
            errorHandler = errorHandler
        )
        // then
        assertThat(isConnected).isFalse()
    }

    @Test
    fun shouldNotTransformHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITHOUT_HTTP)
        // then
        assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpProtocolFromHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITH_HTTP)
        // then
        assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldRemoveHttpsProtocolFromHost() { // when
        val transformedHost =
            strategy.adjustHost(HOST_WITH_HTTP)
        // then
        assertThat(transformedHost)
            .isEqualTo(HOST_WITHOUT_HTTP)
    }

    @Test
    fun shouldAdjustHostDuringCheckingConnectivity() = runTest {
        // given
        val host = host
        every {
            strategy.isConnected(
                host = host,
                port = PORT,
                timeoutInMs = TIMEOUT_IN_MS,
                errorHandler = errorHandler
            )
        } returns true

        // when

        strategy.observeInternetConnectivity(
            initialIntervalInMs = INITIAL_INTERVAL_IN_MS,
            intervalInMs = INTERVAL_IN_MS,
            host = host,
            port = PORT,
            timeoutInMs = TIMEOUT_IN_MS,
            httpResponse = HTTP_RESPONSE,
            errorHandler = errorHandler
        ).test {
            cancelAndConsumeRemainingEvents()
        }
        // then
        verify { strategy.adjustHost(host) }
    }

    companion object {
        private const val INITIAL_INTERVAL_IN_MS = 0
        private const val INTERVAL_IN_MS = 2000
        private const val PORT = 80
        private const val TIMEOUT_IN_MS = 30
        private const val HTTP_RESPONSE = 204
        private const val HOST_WITH_HTTP = "http://www.website.com"
        private const val HOST_WITHOUT_HTTP = "www.website.com"
    }
}
