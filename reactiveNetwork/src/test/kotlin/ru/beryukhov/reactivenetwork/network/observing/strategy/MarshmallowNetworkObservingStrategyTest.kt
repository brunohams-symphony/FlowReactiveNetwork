package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.Connectivity
import ru.beryukhov.reactivenetwork.base.emission
import ru.beryukhov.reactivenetwork.base.emissionCount
import ru.beryukhov.reactivenetwork.base.emissions
import ru.beryukhov.reactivenetwork.base.expect
import ru.beryukhov.reactivenetwork.base.testIn

@RunWith(RobolectricTestRunner::class)
open class MarshmallowNetworkObservingStrategyTest {

    private val strategy = spyk(MarshmallowNetworkObservingStrategy())

    private val powerManager = mockk<PowerManager>(relaxed = true)
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    private val contextMock = mockk<Context>(relaxed = true)
    private val intent = mockk<Intent>(relaxed = true)
    private val network = mockk<Network>(relaxed = true)

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = spyk(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun shouldObserveConnectivity() = runTest {
        // given
        val context = ApplicationProvider.getApplicationContext<Context>()

        val testFlow = strategy.observeNetworkConnectivity(context).map { it.state }
            .test {
                assertThat(awaitItem()).isEqualTo(NetworkInfo.State.CONNECTED)
            }
    }

    @Test
    fun shouldCallOnError() { 
    // given
        val message = "error message"
        val exception = Exception()
        // when
        strategy.onError(message, exception)
        // then
        verify(exactly = 1) { strategy.onError(message, exception) }
    }

    @Test
    fun shouldTryToUnregisterCallbackOnDispose() = runTest {
        // given
        // when
        strategy.observeNetworkConnectivity(context).test {
            cancelAndConsumeRemainingEvents()
        }

        // then
        verify { strategy.tryToUnregisterCallback(any()) }
    }

    @Test
    fun shouldTryToUnregisterReceiverOnDispose() = runTest {
        // given
        // when
        strategy.observeNetworkConnectivity(context).test {
            cancelAndConsumeRemainingEvents()
        }

        // then
        verify { strategy.tryToUnregisterReceiver(context) }
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleAndIsNotIgnoringBatteryOptimizations() {
        // given
        preparePowerManagerMocks(idleMode = false, ignoreOptimizations = false)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldBeInIdleModeWhenDeviceIsNotIgnoringBatteryOptimizations() {
        // given
        preparePowerManagerMocks(idleMode = true, ignoreOptimizations = false)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        assertThat(isIdleMode).isTrue()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsInIdleModeAndIgnoringBatteryOptimizations() {
        // given
        preparePowerManagerMocks(idleMode = true, ignoreOptimizations = true)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldNotBeInIdleModeWhenDeviceIsNotInIdleMode() { 
    // given
        preparePowerManagerMocks(idleMode = false, ignoreOptimizations = true)
        // when
        val isIdleMode = strategy.isIdleMode(contextMock)
        // then
        assertThat(isIdleMode).isFalse()
    }

    @Test
    fun shouldReceiveIntentInIdleMode() { 
    // given
        preparePowerManagerMocks(idleMode = true, ignoreOptimizations = false)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        verify { strategy.onNext(any()) }
    }


    @Test
    fun shouldReceiveIntentWhenIsNotInIdleMode() { 
    // given
        preparePowerManagerMocks(idleMode = false, ignoreOptimizations = false)
        val broadcastReceiver = strategy.createIdleBroadcastReceiver()
        every { contextMock.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetworkInfo } returns null
        // when
        broadcastReceiver.onReceive(contextMock, intent)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun preparePowerManagerMocks(
        idleMode: Boolean,
        ignoreOptimizations: Boolean
    ) {
        val packageName = "com.github.pwittchen.test"
        every { contextMock.packageName } returns packageName
        every { contextMock.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { powerManager.isDeviceIdleMode } returns idleMode
        every { powerManager.isIgnoringBatteryOptimizations(packageName) } returns ignoreOptimizations
    }

    @Test
    fun shouldCreateNetworkCallbackOnSubscribe() = runTest {
        // when
        strategy.observeNetworkConnectivity(context).test {
            cancelAndConsumeRemainingEvents()
        }

        // then
        verify { strategy.createNetworkCallback(context) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkAvailable() { 
    // given
        val networkCallback = strategy.createNetworkCallback(context)
        // when
        networkCallback.onAvailable(network)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldInvokeOnNextOnNetworkLost() { 
    // given
        val networkCallback = strategy.createNetworkCallback(context)
        // when
        networkCallback.onLost(network)
        // then
        verify { strategy.onNext(any()) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    fun shouldHandleErrorWhileTryingToUnregisterCallback() { 
    // given
        strategy.observeNetworkConnectivity(context)
        val exception = IllegalArgumentException()
        every { connectivityManager.unregisterNetworkCallback(any<NetworkCallback>()) } throws exception
        // when
        strategy.tryToUnregisterCallback(connectivityManager)
        // then
        verify {
            strategy.onError(
                MarshmallowNetworkObservingStrategy.ERROR_MSG_NETWORK_CALLBACK,
                exception
            )
        }
    }

    @Test
    fun shouldHandleErrorWhileTryingToUnregisterReceiver() { 
    // given
        strategy.observeNetworkConnectivity(context)
        val exception = RuntimeException()
        every { contextMock.unregisterReceiver(any()) } throws exception
        // when
        strategy.tryToUnregisterReceiver(contextMock)
        // then
        verify {
            strategy.onError(
                MarshmallowNetworkObservingStrategy.ERROR_MSG_RECEIVER,
                exception
            )
        }
    }

    @Test
    fun shouldPropagateCurrentAndLastConnectivityWhenSwitchingFromWifiToMobile() {
        val lastType = ConnectivityManager.TYPE_WIFI
        val currentType = ConnectivityManager.TYPE_MOBILE
        assertThatConnectivityIsPropagatedDuringChange(lastType, currentType)
    }

    @Test
    fun shouldPropagateCurrentAndLastConnectivityWhenSwitchingFromMobileToWifi() {
        val lastType = ConnectivityManager.TYPE_MOBILE
        val currentType = ConnectivityManager.TYPE_WIFI
        assertThatConnectivityIsPropagatedDuringChange(lastType, currentType)
    }

    private fun assertThatConnectivityIsPropagatedDuringChange(
        lastType: Int, currentType: Int
    ) = runTest {
        // given
        val last = Connectivity(
            type = lastType,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = currentType,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
        // when
        strategy.propagateAnyConnectedState(last, current).test {
            // then
            assertThat(awaitItem()).isEqualTo(current)
            assertThat(awaitItem()).isEqualTo(last)
            cancelAndConsumeRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNotPropagateLastConnectivityEventWhenTypeIsNotChanged() = runTest {
        // given
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
        // when

        strategy.propagateAnyConnectedState(last, current).test {
            // then
            assertThat(awaitItem()).isEqualTo(current)
            awaitComplete()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNotPropagateLastConnectivityWhenWasNotConnected() = runTest {
        // given
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.DISCONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.CONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
        // when
        strategy.propagateAnyConnectedState(last, current).test {
            // then
            assertThat(awaitItem()).isEqualTo(current)
            awaitComplete()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNotPropagateLastConnectivityWhenIsConnected() = runTest {
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.CONNECTED,
            detailedState = NetworkInfo.DetailedState.CONNECTED
        )
        // when
        strategy.propagateAnyConnectedState(last, current).test {
            // then
            assertThat(awaitItem()).isEqualTo(current)
            awaitComplete()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNotPropagateLastConnectivityWhenIsIdle() = runTest {
        // given
        val last = Connectivity(
            type = ConnectivityManager.TYPE_WIFI,
            state = NetworkInfo.State.CONNECTED
        )
        val current = Connectivity(
            type = ConnectivityManager.TYPE_MOBILE,
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = NetworkInfo.DetailedState.IDLE
        )
        // when

        strategy.propagateAnyConnectedState(last, current).test {
            // then
            assertThat(awaitItem()).isEqualTo(current)
            awaitComplete()
        }
    }
}

