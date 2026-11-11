package com.andrerinas.openheadunit.connection.carkey

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OlaVolumeStepPolicyTest {

    @Test
    fun `a step up or down from the middle sends that key`() {
        assertEquals(KeyEvent.KEYCODE_VOLUME_UP, OlaVolumeStepPolicy.keyFor(from = 7, to = 8, mid = 7, recenter = true))
        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, OlaVolumeStepPolicy.keyFor(from = 7, to = 6, mid = 7, recenter = true))
    }

    @Test
    fun `landing on the middle is the snap-back only while re-centring`() {
        assertNull(OlaVolumeStepPolicy.keyFor(from = 8, to = 7, mid = 7, recenter = true))
        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, OlaVolumeStepPolicy.keyFor(from = 8, to = 7, mid = 7, recenter = false))
    }

    @Test
    fun `no change or a missing extra is not a press`() {
        assertNull(OlaVolumeStepPolicy.keyFor(from = 7, to = 7, mid = 7, recenter = true))
        assertNull(OlaVolumeStepPolicy.keyFor(from = -1, to = 8, mid = 7, recenter = true))
        assertNull(OlaVolumeStepPolicy.keyFor(from = 7, to = -1, mid = 7, recenter = true))
    }
}