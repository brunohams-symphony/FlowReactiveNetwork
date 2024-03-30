package ru.beryukhov.reactivenetwork.internet.observing.strategy

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.io.IOException
import java.net.HttpURLConnection
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler

@RunWith(RobolectricTestRunner::class)
class WalledGardenInternetObservingStrategyTest {

    private val errorHandler = mockk<ErrorHandler>(relaxed = true)
    private val strategy = spyk(WalledGardenInternetObservingStrategy())

    private val host: String = strategy.getDefaultPingHost()

    @Test
    fun shouldBeConnectedToTheInternet() = runTest {
        // given
        val errorHandlerStub = createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns true

        // when
        strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        ).test {


            // then
            assertThat(awaitItem()).isEqualTo(true)
        }
    }

    @Test
    fun shouldNotBeConnectedToTheInternet() = runTest {
        // given
        val errorHandlerStub =
            createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns false
        // when
        strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        ).test {

            // then
            assertThat(awaitItem()).isEqualTo(false)
        }
    }

    @Test
    fun shouldBeConnectedToTheInternetViaSingle() = runTest {
        // given
        val errorHandlerStub = createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns true

        // when
        val isConnected = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        )
        // then
        assertThat(isConnected).isTrue()
    }

    @Test
    fun shouldNotBeConnectedToTheInternetViaSingle() = runTest {
        // given
        val errorHandlerStub =
            createErrorHandlerStub()
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns false
        // when
        val isConnected = strategy.checkInternetConnectivity(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        )
        // then
        assertThat(isConnected).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateHttpUrlConnection() {
        // given
        val parsedDefaultHost = "clients3.google.com"
        // when
        val connection = strategy.createHttpUrlConnection(
            host,
            PORT,
            TIMEOUT_IN_MS
        )
        // then
        assertThat(connection).isNotNull()
        assertThat(connection.url.host).isEqualTo(parsedDefaultHost)
        assertThat(connection.url.port).isEqualTo(PORT)
        assertThat(connection.connectTimeout).isEqualTo(TIMEOUT_IN_MS)
        assertThat(connection.readTimeout).isEqualTo(TIMEOUT_IN_MS)
        assertThat(connection.instanceFollowRedirects).isFalse()
        assertThat(connection.useCaches).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionWhileCreatingHttpUrlConnection() {
        // given
        val errorMsg = "Could not establish connection with WalledGardenStrategy"
        val givenException = IOException(errorMsg)
        every {
            strategy.createHttpUrlConnection(
                HOST_WITH_HTTP,
                PORT,
                TIMEOUT_IN_MS
            )
        } throws (givenException)
        // when
        strategy.isConnected(
            HOST_WITH_HTTP,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        // then
        verify { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateHttpsUrlConnection() {
        // given
        val parsedDefaultHost = "clients3.google.com"
        // when
        val connection: HttpURLConnection = strategy.createHttpsUrlConnection(
            "https://clients3.google.com",
            PORT,
            TIMEOUT_IN_MS
        )
        // then
        assertThat(connection).isNotNull()
        assertThat(connection.url.host).isEqualTo(parsedDefaultHost)
        assertThat(connection.url.port).isEqualTo(PORT)
        assertThat(connection.connectTimeout).isEqualTo(TIMEOUT_IN_MS)
        assertThat(connection.readTimeout).isEqualTo(TIMEOUT_IN_MS)
        assertThat(connection.instanceFollowRedirects).isFalse()
        assertThat(connection.useCaches).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun shouldHandleAnExceptionWhileCreatingHttpsUrlConnection() {
        // given
        val errorMsg = "Could not establish connection with WalledGardenStrategy"
        val givenException = IOException(errorMsg)
        val host = "https://clients3.google.com"
        every {
            strategy.createHttpsUrlConnection(
                host,
                PORT,
                TIMEOUT_IN_MS
            )
        } throws (givenException)
        // when
        strategy.isConnected(
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandler
        )
        // then
        verify { errorHandler.handleError(givenException, errorMsg) }
    }

    @Test
    fun shouldNotTransformHttpHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITH_HTTPS)
        // then
        assertThat(transformedHost).isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldNotTransformHttpsHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITH_HTTPS)
        // then
        assertThat(transformedHost)
            .isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldAddHttpsProtocolToHost() { // when
        val transformedHost = strategy.adjustHost(HOST_WITHOUT_HTTPS)
        // then
        assertThat(transformedHost).isEqualTo(HOST_WITH_HTTPS)
    }

    @Test
    fun shouldAdjustHostWhileCheckingConnectivity() = runTest {
        // given
        val errorHandlerStub =
            createErrorHandlerStub()
        val host = host
        every {
            strategy.isConnected(
                host,
                PORT,
                TIMEOUT_IN_MS,
                HTTP_RESPONSE,
                errorHandlerStub
            )
        } returns true

        // when

        strategy.observeInternetConnectivity(
            INITIAL_INTERVAL_IN_MS,
            INTERVAL_IN_MS,
            host,
            PORT,
            TIMEOUT_IN_MS,
            HTTP_RESPONSE,
            errorHandlerStub
        ).test{
            cancelAndConsumeRemainingEvents()
        }
        // then
        verify { strategy.adjustHost(host) }

    }

    private fun createErrorHandlerStub(): ErrorHandler {
        return object : ErrorHandler {
            override fun handleError(
                exception: Exception?,
                message: String?
            ) {
            }
        }
    }

    companion object {
        private const val INITIAL_INTERVAL_IN_MS = 0
        private const val INTERVAL_IN_MS = 2000
        private const val PORT = 80
        private const val TIMEOUT_IN_MS = 30
        private const val HTTP_RESPONSE = 204
        private const val HOST_WITH_HTTP = "http://www.website.com"
        private const val HOST_WITH_HTTPS = "https://www.website.com"
        private const val HOST_WITHOUT_HTTPS = "www.website.com"
    }
}
