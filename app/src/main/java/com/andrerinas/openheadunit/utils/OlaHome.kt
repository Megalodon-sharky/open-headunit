package com.andrerinas.openheadunit.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent

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

    fun launch(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
            .setComponent(MOVE_OS_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (context.packageManager.resolveActivity(intent, 0) == null) return false
        return try {
            context.startActivity(intent)
            AppLog.i("OlaHome: started the MoveOS home screen")
            true
        } catch (e: Exception) {
            AppLog.w("OlaHome: could not start the MoveOS home screen: ${e.message}")
            false
        }
    }
}