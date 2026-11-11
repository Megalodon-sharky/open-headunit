package com.andrerinas.openheadunit.connection.carkey

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.andrerinas.openheadunit.App
import com.andrerinas.openheadunit.utils.AppLog
import com.andrerinas.openheadunit.utils.OlaHome
import com.andrerinas.openheadunit.utils.Settings

/**
 * Ola S1 build only: the handlebar Vol+ and Vol- never become key events. The switch cube goes
 * over CAN to Ola's VDP service, and com.android.hmiserverapp then calls
 * AudioManager.setStreamVolume(STREAM_MUSIC, index ± 1). So during a session on an Ola dash with
 * Send Volume Buttons To The Phone on, this watches STREAM_MUSIC steps and sends each one to the
 * phone as a volume key. With [OlaVolumeStepPolicy.RECENTER] it then puts the dash back to the
 * middle so presses never stall at its minimum or maximum. The rider listens on the phone, so the
 * dash level does not matter during the session, and it is restored when the session ends.
 */
class OlaVolumeReceiver : BroadcastReceiver(), CarKeyReceiver {

    private var context: Context? = null
    private var audioManager: AudioManager? = null
    private var mid = -1
    private var restore = -1

    override val isSupported = true
    override val isSUNeeded = false
    override val isSessionScoped = true

    override fun register(context: Context) {
        // Other head units keep their own volume handling, and so does an Ola rider who left the
        // setting off.
        if (!OlaHome.isPresent(context) || !Settings(context).volumeKeysToPhone) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        restore = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        mid = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
        if (OlaVolumeStepPolicy.RECENTER) setMusicVolume(audio, mid)
        // Only the framework can send this protected broadcast, so exporting the receiver is safe.
        ContextCompat.registerReceiver(
            context,
            this,
            IntentFilter(VOLUME_CHANGED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
        this.context = context
        audioManager = audio
        AppLog.i("OlaVolumeReceiver: armed (mid=$mid, restore=$restore, recenter=${OlaVolumeStepPolicy.RECENTER})")
    }

    override fun unregister() {
        val registeredOn = context ?: return
        try {
            registeredOn.unregisterReceiver(this)
        } catch (_: IllegalArgumentException) {
        }
        val audio = audioManager
        if (OlaVolumeStepPolicy.RECENTER && audio != null && restore >= 0) setMusicVolume(audio, restore)
        context = null
        audioManager = null
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.getIntExtra(EXTRA_STREAM_TYPE, -1) != AudioManager.STREAM_MUSIC) return
        val key = OlaVolumeStepPolicy.keyFor(
            from = intent.getIntExtra(EXTRA_PREV_VALUE, -1),
            to = intent.getIntExtra(EXTRA_VALUE, -1),
            mid = mid,
        ) ?: return
        AppLog.i("OlaVolumeReceiver: dash volume step, sending keycode $key to the phone")
        // The same route as the volume buttons in AapProjectionActivity. A fresh downTime makes each
        // step its own press for KeyDebouncePolicy; without one, a second press inside its 300 ms
        // window would be dropped as the first arriving twice.
        val commManager = App.provide(context).commManager
        val downTime = SystemClock.uptimeMillis()
        commManager.sendKey(key, true, downTime, "ola-handlebar")
        commManager.sendKey(key, false, downTime, "ola-handlebar")
        if (OlaVolumeStepPolicy.RECENTER) audioManager?.let { setMusicVolume(it, mid) }
    }

    private fun setMusicVolume(audio: AudioManager, index: Int) {
        try {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
        } catch (e: SecurityException) {
            AppLog.w("OlaVolumeReceiver: could not set the dash volume: ${e.message}")
        }
    }

    private companion object {
        // AudioManager's hidden names for the broadcast the framework sends on every volume change.
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        const val EXTRA_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
        const val EXTRA_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE"
        const val EXTRA_PREV_VALUE = "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE"
    }
}