package ru.beryukhov.reactivenetwork.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import org.junit.Rule
import kotlin.test.assertEquals

abstract class BaseFlowTest(overrideMainDispatcher: Boolean = false) {
    @get:Rule
    val testScopeRule = TestCoroutineScopeRule(overrideMainDispatcher)

    /*@After
    fun cleanup() {
        testScopeRule.cleanupTestCoroutines()
    }*/

    suspend fun <T> Flow<T>.expectFirst(expected: T) {
        launchIn(testScopeRule)
        assertEquals(expected, first())
    }
}