package ru.beryukhov.reactivenetwork.internet.observing

import ru.beryukhov.reactivenetwork.internet.observing.error.DefaultErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.error.ErrorHandler
import ru.beryukhov.reactivenetwork.internet.observing.strategy.WalledGardenInternetObservingStrategy
import java.net.HttpURLConnection

/**
 * Contains state of internet connectivity settings.
 * We should use its Builder for creating new settings
 */
// I want to have the same method names as variable names on purpose
public class InternetObservingSettings private constructor(
    private val initialInterval: Int,
    private val interval: Int,
    private val host: String,
    private val port: Int,
    private val timeout: Int,
    private val httpResponse: Int,
    private val errorHandler: ErrorHandler,
    private val strategy: InternetObservingStrategy
) {

    private constructor(builder: Builder = builder()) : this(
        builder.initialInterval, builder.interval, builder.host, builder.port, builder.timeout,
        builder.httpResponse, builder.errorHandler, builder.strategy
    )

    /**
     * @return initial ping interval in milliseconds
     */
    public fun initialInterval(): Int {
        return initialInterval
    }

    /**
     * @return ping interval in milliseconds
     */
    public fun interval(): Int {
        return interval
    }

    /**
     * @return ping host
     */
    public fun host(): String {
        return host
    }

    /**
     * @return ping port
     */
    public fun port(): Int {
        return port
    }

    /**
     * @return ping timeout in milliseconds
     */
    public fun timeout(): Int {
        return timeout
    }

    public fun httpResponse(): Int {
        return httpResponse
    }

    /**
     * @return error handler for pings and connections
     */
    public fun errorHandler(): ErrorHandler {
        return errorHandler
    }

    /**
     * @return internet observing strategy
     */
    public fun strategy(): InternetObservingStrategy {
        return strategy
    }

    /**
     * Settings builder, which contains default parameters
     */
    public class Builder internal constructor() {
        internal var initialInterval = 0
        internal var interval = 2000
        internal var host = "http://clients3.google.com/generate_204"
        internal var port = 80
        internal var timeout = 2000
        internal var httpResponse = HttpURLConnection.HTTP_NO_CONTENT
        internal var errorHandler: ErrorHandler =
            DefaultErrorHandler()
        internal var strategy: InternetObservingStrategy = WalledGardenInternetObservingStrategy()
        /**
         * sets initial ping interval in milliseconds
         *
         * @param initialInterval in milliseconds
         * @return Builder
         */
        public fun initialInterval(initialInterval: Int): Builder {
            this.initialInterval = initialInterval
            return this
        }

        /**
         * sets ping interval in milliseconds
         *
         * @param interval in milliseconds
         * @return Builder
         */
        public fun interval(interval: Int): Builder {
            this.interval = interval
            return this
        }

        /**
         * sets ping host
         *
         * @return Builder
         */
        public fun host(host: String): Builder {
            this.host = host
            return this
        }

        /**
         * sets ping port
         *
         * @return Builder
         */
        public fun port(port: Int): Builder {
            this.port = port
            return this
        }

        /**
         * sets ping timeout in milliseconds
         *
         * @param timeout in milliseconds
         * @return Builder
         */
        public fun timeout(timeout: Int): Builder {
            this.timeout = timeout
            return this
        }

        /**
         * sets HTTP response code indicating that connection is established
         *
         * @param httpResponse as integer
         * @return Builder
         */
        public fun httpResponse(httpResponse: Int): Builder {
            this.httpResponse = httpResponse
            return this
        }

        /**
         * sets error handler for pings and connections
         *
         * @return Builder
         */
        public fun errorHandler(errorHandler: ErrorHandler): Builder {
            this.errorHandler = errorHandler
            return this
        }

        /**
         * sets internet observing strategy
         *
         * @param strategy for observing and internet connection
         * @return Builder
         */
        public fun strategy(strategy: InternetObservingStrategy): Builder {
            this.strategy = strategy
            return this
        }

        public fun build(): InternetObservingSettings {
            return InternetObservingSettings(this)
        }
    }

    public companion object {
        /**
         * @return settings with default parameters
         */
        public fun create(): InternetObservingSettings {
            return Builder()
                .build()
        }

        /**
         * Creates builder object
         * @return Builder
         */
        public fun builder(): Builder {
            return Builder()
        }
    }

}