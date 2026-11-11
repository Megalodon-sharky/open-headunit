package com.andrerinas.openheadunit.connection.carkey

import android.view.KeyEvent

/** Turns one STREAM_MUSIC change on the Ola dash into the volume key for the phone; see [OlaVolumeReceiver]. */
object OlaVolumeStepPolicy {

    /**
     * Whether the dash goes back to the middle after each step. Right only if hmiserverapp steps
     * from the current index. If it keeps its own counter instead, re-centring flips directions:
     * set this to false, and presses then stop registering at the dash's minimum and maximum.
     * Not yet checked on a scooter: `cmd media_session volume --stream 3 --set 5`, press Vol+, then
     * `dumpsys audio | grep setStreamVol`; index 6 from hmiserverapp means true is right.
     */
    const val RECENTER = true

    /** The key for a change from [from] to [to], or null when the change is not a press. */
    fun keyFor(from: Int, to: Int, mid: Int, recenter: Boolean = RECENTER): Int? {
        if (from < 0 || to < 0 || to == from) return null
        // Landing on the middle is the receiver's own snap-back, or its set at session start.
        if (recenter && to == mid) return null
        return if (to > from) KeyEvent.KEYCODE_VOLUME_UP else KeyEvent.KEYCODE_VOLUME_DOWN
    }
}