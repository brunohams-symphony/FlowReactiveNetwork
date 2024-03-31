package ru.beryukhov.reactivenetwork.internet.observing.error

public interface ErrorHandler {
    public fun handleError(exception: Exception?, message: String?)
}
