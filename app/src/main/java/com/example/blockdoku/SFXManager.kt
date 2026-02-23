package com.example.blockdoku

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SFXManager {

    private var soundPool: SoundPool? = null
    private var clickSound = 0
    private var errorSound = 0
    private var bombSound = 0
    private var clearSound = 0
    private var LineClearSound = 0
    private var loaded = false

    fun init(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        clickSound = soundPool!!.load(context, R.raw.click_sound, 1)
        errorSound = soundPool!!.load(context, R.raw.error_sound, 1)
        bombSound = soundPool!!.load(context, R.raw.bomb_sound, 1)
        clearSound = soundPool!!.load(context, R.raw.clear_sound, 1)
        LineClearSound = soundPool!!.load(context, R.raw.line_clear_sound, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }
    }

    private fun isSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("sound_enabled", true)
    }

    fun playClick(context: Context) {
        if (loaded && isSoundEnabled(context)) {
            soundPool?.play(clickSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playError(context: Context) {
        if (loaded && isSoundEnabled(context)) {
            soundPool?.play(errorSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playBomb(context: Context) {
        if (loaded && isSoundEnabled(context)) {
            soundPool?.play(bombSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playClear(context: Context) {
        if (loaded && isSoundEnabled(context)) {
            soundPool?.play(clearSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playLineClear(context: Context) {
        if (loaded && isSoundEnabled(context)) {
            soundPool?.play(LineClearSound, 1f, 1f, 1, 0, 1f)
        }
    }
}