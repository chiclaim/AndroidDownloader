package com.chiclaim.android.updater

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadExecutor {

    companion object {
        private val execute = ThreadPoolExecutor(
            1,
            1,
            2,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(100),
            { r -> Thread(r, "DownloadProgressTask") },
            ThreadPoolExecutor.DiscardOldestPolicy()
        )

        init {
            execute.allowCoreThreadTimeOut(true)
        }

        fun execute(runnable: Runnable) {
            execute.execute(runnable)
        }

    }



}