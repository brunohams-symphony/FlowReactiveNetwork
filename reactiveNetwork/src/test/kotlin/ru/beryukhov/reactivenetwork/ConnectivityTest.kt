package ru.beryukhov.reactivenetwork

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkInfo.DetailedState
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.beryukhov.reactivenetwork.Connectivity.Companion.create
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.appendUnknownNetworkTypeToTypes
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.hasState
import ru.beryukhov.reactivenetwork.ConnectivityPredicate.hasType

@RunWith(RobolectricTestRunner::class)
class ConnectivityTest {
    @Test
    fun shouldCreateConnectivity() { // when
        val connectivity = Connectivity()
        // then
        assertThat(connectivity).isNotNull()
        assertThat(connectivity.state)
            .isEqualTo(NetworkInfo.State.DISCONNECTED)
        assertThat(connectivity.detailedState)
            .isEqualTo(DetailedState.IDLE)
        assertThat(connectivity.type).isEqualTo(Connectivity.UNKNOWN_TYPE)
        assertThat(connectivity.subType).isEqualTo(Connectivity.UNKNOWN_SUB_TYPE)
        assertThat(connectivity.available).isFalse()
        assertThat(connectivity.failover).isFalse()
        assertThat(connectivity.roaming).isFalse()
        assertThat(connectivity.typeName)
            .isEqualTo(TYPE_NAME_NONE)
        assertThat(connectivity.subTypeName)
            .isEqualTo(TYPE_NAME_NONE)
        assertThat(connectivity.reason).isEmpty()
        assertThat(connectivity.extraInfo).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldBeEqualToGivenValue() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.CONNECTED,
            type = ConnectivityManager.TYPE_WIFI,
            typeName = TYPE_NAME_WIFI
        )

        // when
        val equalTo =
            hasState(connectivity.state)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldBeEqualToOneOfGivenMultipleValues() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.CONNECTING,
            type = ConnectivityManager.TYPE_WIFI,
            typeName = TYPE_NAME_WIFI
        )

        val states = arrayOf(NetworkInfo.State.CONNECTED, NetworkInfo.State.CONNECTING)
        // when
        val equalTo = hasState(*states)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun stateShouldNotBeEqualToGivenValue() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.DISCONNECTED,
            type = ConnectivityManager.TYPE_WIFI,
            typeName = TYPE_NAME_WIFI
        )

        // when
        val equalTo = hasState(NetworkInfo.State.CONNECTED)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldBeEqualToGivenValue() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.CONNECTED,
            type = ConnectivityManager.TYPE_WIFI,
            typeName = TYPE_NAME_WIFI
        )
        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(connectivity.type, Connectivity.UNKNOWN_TYPE)
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldBeEqualToOneOfGivenMultipleValues() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.CONNECTING,
            type = ConnectivityManager.TYPE_MOBILE,
            typeName = TYPE_NAME_MOBILE
        )

        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_MOBILE,
            Connectivity.UNKNOWN_TYPE
        )
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun typeShouldNotBeEqualToGivenValue() {
        // given
        val connectivity = Connectivity(
            state = NetworkInfo.State.CONNECTED,
            type = ConnectivityManager.TYPE_WIFI,
            typeName = TYPE_NAME_WIFI
        )

        // note that unknown type is added initially by the ConnectivityPredicate#hasType method
        val givenTypes = intArrayOf(ConnectivityManager.TYPE_MOBILE, Connectivity.UNKNOWN_TYPE)
        // when
        val equalTo = hasType(*givenTypes)
        val shouldBeEqualToGivenStatus = equalTo.test(connectivity)
        // then
        assertThat(shouldBeEqualToGivenStatus).isFalse()
    }

    @Test
    fun theSameConnectivityObjectsShouldBeEqual() {
        // given
        val connectivityOne = Connectivity()
        val connectivityTwo = Connectivity()
        // when
        val objectsAreEqual = connectivityOne == connectivityTwo
        // then
        assertThat(objectsAreEqual).isTrue()
    }

    @Test
    fun twoDefaultObjectsShouldBeInTheSameBucket() {
        // given
        val connectivityOne = Connectivity()
        val connectivityTwo = Connectivity()
        // when
        val hashCodesAreEqual = connectivityOne.hashCode() == connectivityTwo.hashCode()
        // then
        assertThat(hashCodesAreEqual).isTrue()
    }

    @Test
    fun shouldAppendUnknownTypeWhileFilteringNetworkTypesInsidePredicate() {
        // given
        val types =
            intArrayOf(ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI)
        val expectedOutputTypes = intArrayOf(
            ConnectivityManager.TYPE_MOBILE,
            ConnectivityManager.TYPE_WIFI,
            Connectivity.UNKNOWN_TYPE
        )
        // when
        val outputTypes =
            appendUnknownNetworkTypeToTypes(types)
        // then
        assertThat(outputTypes).isEqualTo(expectedOutputTypes)
    }

    @Test
    fun shouldAppendUnknownTypeWhileFilteringNetworkTypesInsidePredicateForEmptyArray() {
        // given
        val types = intArrayOf()
        val expectedOutputTypes = intArrayOf(Connectivity.UNKNOWN_TYPE)
        // when
        val outputTypes = appendUnknownNetworkTypeToTypes(types)
        // then
        assertThat(outputTypes).isEqualTo(expectedOutputTypes)
    }

    @Test
    fun shouldCreateConnectivityWithBuilder() {
        // given
        val state = NetworkInfo.State.CONNECTED
        val detailedState = DetailedState.CONNECTED
        val type = ConnectivityManager.TYPE_WIFI
        val subType = ConnectivityManager.TYPE_WIMAX
        val typeName = TYPE_NAME_WIFI
        val subTypeName = "test subType"
        val reason = "no reason"
        val extraInfo = "extra info"
        // when
        val connectivity = Connectivity(
            state = state,
            detailedState = detailedState,
            type = type,
            subType = subType,
            available = true,
            failover = false,
            roaming = true,
            typeName = typeName,
            subTypeName = subTypeName,
            reason = reason,
            extraInfo = extraInfo
        )

        // then
        assertThat(connectivity.state).isEqualTo(state)
        assertThat(connectivity.detailedState).isEqualTo(detailedState)
        assertThat(connectivity.type).isEqualTo(type)
        assertThat(connectivity.subType).isEqualTo(subType)
        assertThat(connectivity.available).isTrue()
        assertThat(connectivity.failover).isFalse()
        assertThat(connectivity.roaming).isTrue()
        assertThat(connectivity.typeName).isEqualTo(typeName)
        assertThat(connectivity.subTypeName).isEqualTo(subTypeName)
        assertThat(connectivity.reason).isEqualTo(reason)
        assertThat(connectivity.extraInfo).isEqualTo(extraInfo)
    }

    @Test
    fun connectivityShouldNotBeEqualToAnotherOne() {
        // given
        val connectivityOne = Connectivity(
            state = NetworkInfo.State.CONNECTED,
            detailedState = DetailedState.CONNECTED,
            type = ConnectivityManager.TYPE_WIFI,
            subType = 1,
            available = true,
            failover = true,
            roaming = true,
            typeName = TYPE_NAME_WIFI,
            subTypeName = "subtypeOne",
            reason = "reasonOne",
            extraInfo = "extraInfoOne"
        )

        val connectivityTwo = Connectivity(
            state = NetworkInfo.State.DISCONNECTED,
            detailedState = DetailedState.DISCONNECTED,
            type = ConnectivityManager.TYPE_MOBILE,
            subType = 2,
            available = false,
            failover = false,
            roaming = false,
            typeName = TYPE_NAME_MOBILE,
            subTypeName = "subtypeTwo",
            reason = "reasonTwo",
            extraInfo = "extraInfoTwo"
        )
        // when
        val isAnotherConnectivityTheSame = connectivityOne == connectivityTwo
        // then
        assertThat(isAnotherConnectivityTheSame).isFalse()
    }

    @Test
    fun shouldCreateDefaultConnectivityWhenConnectivityManagerIsNull() {
        // given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val connectivityManager: ConnectivityManager? = null
        // when
        val connectivity = create(context, connectivityManager)
        // then
        assertThat(connectivity.type).isEqualTo(Connectivity.UNKNOWN_TYPE)
        assertThat(connectivity.subType).isEqualTo(Connectivity.UNKNOWN_SUB_TYPE)
        assertThat(connectivity.state)
            .isEqualTo(NetworkInfo.State.DISCONNECTED)
        assertThat(connectivity.detailedState)
            .isEqualTo(DetailedState.IDLE)
        assertThat(connectivity.available).isFalse()
        assertThat(connectivity.failover).isFalse()
        assertThat(connectivity.roaming).isFalse()
        assertThat(connectivity.typeName)
            .isEqualTo(TYPE_NAME_NONE)
        assertThat(connectivity.subTypeName)
            .isEqualTo(TYPE_NAME_NONE)
        assertThat(connectivity.reason).isEmpty()
        assertThat(connectivity.extraInfo).isEmpty()
    }

    companion object {
        private const val TYPE_NAME_WIFI = "WIFI"
        private const val TYPE_NAME_MOBILE = "MOBILE"
        private const val TYPE_NAME_NONE = "NONE"
    }
}
