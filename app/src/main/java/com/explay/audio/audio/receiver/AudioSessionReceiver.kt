package com.explay.audio.audio.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log
import com.explay.audio.audio.engine.AudioEngineManager

class AudioSessionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Unknown"

        Log.i(TAG, "Audio Session event: $action dari paket: $packageName (session ID: $sessionId)")

        if (sessionId == AudioEffect.ERROR_BAD_VALUE) return

        val engine = AudioEngineManager.getInstance(context)

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                engine.onSessionOpened(sessionId)
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                engine.onSessionClosed(sessionId)
            }
        }
    }

    companion object {
        private const val TAG = "AudioSessionReceiver"
    }
}
