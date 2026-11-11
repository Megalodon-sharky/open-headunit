package com.andrerinas.openheadunit.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.andrerinas.openheadunit.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Ola S1 build only. The scooter's own home screen (MoveOS) is an ordinary app, not the system
 * home app, so the home intent and a plain exit leave the rider on the stock launcher. [launch]
 * starts MoveOS instead, the same as `am start -n com.olaelectric.hmiclient/.MainActivity`, and
 * returns false on any other device, where the caller keeps its usual behaviour. The manifest
 * lists the package under <queries> so it resolves on Android 11 and later.
 */
object OlaHome {
    private val MOVE_OS_HOME =
        ComponentName("com.olaelectric.hmiclient", "com.olaelectric.hmiclient.MainActivity")

    /** Whether this is an Ola dash, judged by its MoveOS home screen being installed. */
    fun isPresent(context: Context): Boolean =
        context.packageManager.resolveActivity(moveOsIntent(), 0) != null

    fun launch(context: Context): Boolean {
        if (!isPresent(context)) return false
        return try {
            context.startActivity(moveOsIntent())
            AppLog.i("OlaHome: started the MoveOS home screen")
            true
        } catch (e: Exception) {
            AppLog.w("OlaHome: could not start the MoveOS home screen: ${e.message}")
            false
        }
    }

    /**
     * The Ola S1 Bluetooth pairing steps, shown once: the first time this app opens on an Ola dash.
     * The flag lives in its own preferences file, outside the backed-up settings.
     */
    fun showPairingGuideOnce(context: Context) {
        if (!isPresent(context)) return
        val prefs = context.getSharedPreferences("ola_s1", Context.MODE_PRIVATE)
        if (prefs.getBoolean("pairing-guide-shown", false)) return
        prefs.edit().putBoolean("pairing-guide-shown", true).apply()
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.ola_pairing_guide_title)
            .setMessage(
                HtmlCompat.fromHtml(
                    context.getString(R.string.ola_pairing_guide_message),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /** This build's own code: the Ola S1 branch of Megalodon's fork, which the home card's credit opens. */
    const val CREDIT_URL = "https://github.com/Megalodon-sharky/open-headunit/tree/ola-s1-pro"

    fun openCredit(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(CREDIT_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            // A dash without a browser: show the address instead.
            ToastUtils.showToast(context, CREDIT_URL.removePrefix("https://"), Toast.LENGTH_LONG, force = true)
        }
    }

    private fun moveOsIntent() = Intent(Intent.ACTION_MAIN)
        .setComponent(MOVE_OS_HOME)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}