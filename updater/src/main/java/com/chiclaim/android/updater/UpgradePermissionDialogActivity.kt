package com.chiclaim.android.updater

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.updater.util.hasInstallPermission
import com.chiclaim.android.updater.util.settingPackageInstall
import com.chiclaim.android.updater.util.startInstall
import java.lang.IllegalArgumentException

/**
 *
 * @author by chiclaim@google.com
 */
class UpgradePermissionDialogActivity : AppCompatActivity() {

    lateinit var uri: String

    companion object {
        const val EXTRA_URI = "extra_uri_path"

        fun launch(context: Context, filePath: String) {
            val intent = Intent(context, UpgradePermissionDialogActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(EXTRA_URI, filePath)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_dialog_layout)
        uri = intent.getStringExtra(EXTRA_URI)
            ?: throw IllegalArgumentException("mission EXTRA_FILE_PATH")

        findViewById<View>(R.id.tv_permission_confirm).setOnClickListener {
            settingPackageInstall(this, 100)
        }
        findViewById<View>(R.id.tv_permission_cancel).setOnClickListener {
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        if (hasInstallPermission(this)) {
            startInstall(this, Uri.parse(uri))
            finish()
        }
    }
}