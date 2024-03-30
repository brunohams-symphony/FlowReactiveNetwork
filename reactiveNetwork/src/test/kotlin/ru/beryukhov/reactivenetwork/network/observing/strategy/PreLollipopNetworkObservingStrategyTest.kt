package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.BroadcastReceiver
import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.base.emission
import ru.beryukhov.reactivenetwork.base.expect
import ru.beryukhov.reactivenetwork.base.testIn
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

@RunWith(RobolectricTestRunner::class)
open class PreLollipopNetworkObservingStrategyTest {

    //@OptIn(ExperimentalCoroutinesApi::class)
    @Ignore
    @Test
    fun shouldObserveConnectivity() = runTest {
        // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        val context = ApplicationProvider.getApplicationContext<Context>()
        // when
        strategy.observeNetworkConnectivity(context).map { it.state }.test {
            delay(1000)
            // then
            assertThat(awaitItem()).isEqualTo(NetworkInfo.State.CONNECTED)
        }
    }

    @Test
    fun shouldCallOnError() {
        // given
        val message = "error message"
        val exception = Exception()
        val strategy = spyk(PreLollipopNetworkObservingStrategy())
        // when
        strategy.onError(message, exception)
        // then
        verify(exactly = 1) { strategy.onError(message, exception) }
    }

    @Test
    fun shouldTryToUnregisterReceiver() {
        // given
        val strategy = PreLollipopNetworkObservingStrategy()
        val context = spyk(ApplicationProvider.getApplicationContext())
        val broadcastReceiver = mockk<BroadcastReceiver>(relaxed = true)
        // when
        strategy.tryToUnregisterReceiver(context, broadcastReceiver)
        // then
        verify { context.unregisterReceiver(broadcastReceiver) }
    }

    //@OptIn(ExperimentalCoroutinesApi::class)
    @Ignore
    @Test
    fun shouldTryToUnregisterReceiverAfterDispose() = runTest {
        // given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val strategy = spyk(PreLollipopNetworkObservingStrategy())
        // when

        strategy.observeNetworkConnectivity(context).test {
            cancel()
        }
        // then
        verify { strategy.tryToUnregisterReceiver(context, any()) }
    }
}

