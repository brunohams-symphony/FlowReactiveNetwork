package ru.beryukhov.reactivenetwork.network.observing.strategy

import android.content.Context
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.network.observing.NetworkObservingStrategy

@RunWith(RobolectricTestRunner::class)
class LollipopNetworkObservingStrategyTest {

    @Test
    fun shouldObserveConnectivity() = runTest {
        // given
        val strategy: NetworkObservingStrategy = LollipopNetworkObservingStrategy()
        val context = ApplicationProvider.getApplicationContext<Context>()

        strategy.observeNetworkConnectivity(context).map { it.state }.test {
            assertThat(awaitItem()).isEqualTo(NetworkInfo.State.CONNECTED)
        }
    }

    @Test
    fun shouldCallOnError() {
        // given
        val message = "error message"
        val exception = Exception()
        val strategy = spyk(LollipopNetworkObservingStrategy())
        // when
        strategy.onError(message, exception)
        // then
        verify(exactly = 1) { strategy.onError(message, exception) }
    }
}
