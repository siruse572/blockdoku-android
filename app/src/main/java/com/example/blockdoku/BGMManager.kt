package com.example.blockdoku

import android.content.Context
import android.media.MediaPlayer

object BGMManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentBGM: Int = -1
    private var isMuted = false  // 음소거 상태

    // 음소거 설정
    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    // 음소거 상태 가져오기
    fun isMuted(): Boolean = isMuted

    // BGM 시작
    fun playBGM(context: Context, resId: Int) {
        // 음소거 상태면 재생 안 함
        if (isMuted) {
            currentBGM = resId
            return
        }

        // 이미 같은 BGM이 재생 중이면 무시
        if (currentBGM == resId && mediaPlayer?.isPlaying == true) {
            return
        }

        // 기존 BGM 정지
        stopBGM()

        // 새 BGM 재생
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            start()
        }
        currentBGM = resId
    }

    // BGM 정지
    fun stopBGM() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentBGM = -1
    }

    // BGM 일시정지
    fun pauseBGM() {
        mediaPlayer?.pause()
    }

    // BGM 재개
    fun resumeBGM() {
        if (!isMuted) {
            mediaPlayer?.start()
        }
    }
}