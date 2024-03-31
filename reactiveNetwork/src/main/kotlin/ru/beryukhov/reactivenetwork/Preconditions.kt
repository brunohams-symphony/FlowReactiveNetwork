package ru.beryukhov.reactivenetwork

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

public object Preconditions {
    /**
     * Validation method, which checks if an object is null
     *
     * @param o object to verify
     * @param message to be thrown in exception
     */
    public fun checkNotNull(o: Any?, message: String) {
        if (o == null) {
            throw IllegalArgumentException(message)
        }
    }

    /**
     * Validation method, which checks if a string is null or empty
     *
     * @param string to verify
     * @param message to be thrown in exception
     */
    public fun checkNotNullOrEmpty(string: String?, message: String) {
        if (string.isNullOrEmpty()) {
            throw IllegalArgumentException(message)
        }
    }

    /**
     * Validation method, which checks is an integer number is positive
     *
     * @param number integer to verify
     * @param message to be thrown in exception
     */
    public fun checkGreaterOrEqualToZero(number: Int, message: String) {
        if (number < 0) {
            throw IllegalArgumentException(message)
        }
    }

    /**
     * Validation method, which checks is an integer number is non-zero or positive
     *
     * @param number integer to verify
     * @param message to be thrown in exception
     */
    public fun checkGreaterThanZero(number: Int, message: String) {
        if (number <= 0) {
            throw IllegalArgumentException(message)
        }
    }

    /**
     * Validation method, which checks if current Android version is at least Lollipop (API 21) or
     * higher
     *
     * @return boolean true if current Android version is Lollipop or higher
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP)
    public fun isAtLeastAndroidLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    /**
     * Validation method, which checks if current Android version is at least Marshmallow (API 23) or
     * higher
     *
     * @return boolean true if current Android version is Marshmallow or higher
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
    public fun isAtLeastAndroidMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}
