package com.chiclaim.android.updater

/**
 *
 * @author by chiclaim@google.com
 */
internal class DownloadException(val errorType: Int, errMsg: String, val responseCode: Int = 0) :
    Exception(errMsg) {
    companion object {

        const val ERROR_NO_NETWORK = 1



        const val ERROR_DM_FAILED = 3
        const val ERROR_MISSING_URI = 4
        const val ERROR_TOO_MANY_REDIRECTS = 5

        const val ERROR_CANNOT_RESUME = 6

        const val ERROR_MISSING_LOCATION_WHEN_REDIRECT = 7
        const val ERROR_UNHANDLED = 8

    }
}