package ru.beryukhov.reactivenetwork.network.observing

import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.network.observing.strategy.LollipopNetworkObservingStrategy
import ru.beryukhov.reactivenetwork.network.observing.strategy.PreLollipopNetworkObservingStrategy

@RunWith(RobolectricTestRunner::class)
class NetworkObservingStrategyTest {
    @Test
    fun lollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() {
        // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }


    @Test
    fun preLollipopObserveNetworkConnectivityShouldBeConnectedWhenNetworkIsAvailable() {
        // given
        val strategy: NetworkObservingStrategy = PreLollipopNetworkObservingStrategy()
        // when
        assertThatIsConnected(strategy)
    }

    private fun assertThatIsConnected(strategy: NetworkObservingStrategy) = runTest {
        // given
        val context = ApplicationProvider.getApplicationContext<Context>()
        //when
        strategy.observeNetworkConnectivity(context).map { it.state }.test {
            // then
            assertThat(awaitItem()).isEqualTo(NetworkInfo.State.CONNECTED)
        }
    }
}
