package ru.beryukhov.reactivenetwork

import android.net.NetworkInfo

/**
 * ConnectivityPredicate is a class containing predefined methods, which can be used for filtering
 * reactive streams of network connectivity
 */
public object ConnectivityPredicate {
    /**
     * Filter, which returns true if at least one given state occurred
     *
     * @param states NetworkInfo.State, which can have one or more states
     * @return true if at least one given state occurred
     */
    @JvmStatic
    public fun hasState(vararg states: NetworkInfo.State): Predicate<Connectivity> {
        return object : Predicate<Connectivity> {
            @Throws(Exception::class)
            override fun test(connectivity: Connectivity): Boolean {
                for (state in states) {
                    if (connectivity.state == state) {
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * Filter, which returns true if at least one given type occurred
     *
     * @param types int, which can have one or more types
     * @return true if at least one given type occurred
     */
    @JvmStatic
    public fun hasType(vararg types: Int): Predicate<Connectivity> {
        val extendedTypes =
            appendUnknownNetworkTypeToTypes(types)
        return object : Predicate<Connectivity> {
            @Throws(Exception::class)
            override fun test(connectivity: Connectivity): Boolean {
                for (type in extendedTypes) {
                    if (connectivity.type == type) {
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * Returns network types from the input with additional unknown type,
     * what helps during connections filtering when device
     * is being disconnected from a specific network
     *
     * @param types of the network as an array of ints
     * @return types of the network with unknown type as an array of ints
     */
    @JvmStatic
    public fun appendUnknownNetworkTypeToTypes(types: IntArray): IntArray {
        var i = 0
        val extendedTypes = IntArray(types.size + 1)
        for (type in types) {
            extendedTypes[i] = type
            i++
        }
        extendedTypes[i] = Connectivity.UNKNOWN_TYPE
        return extendedTypes
    }
}