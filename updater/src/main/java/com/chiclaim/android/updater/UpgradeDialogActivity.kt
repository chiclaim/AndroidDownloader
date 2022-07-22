package com.chiclaim.android.updater

import android.app.Activity
import android.os.Bundle

/**
 *
 * @author by chiclaim@google.com
 */
class UpgradeDialogActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_layout)
    }
}