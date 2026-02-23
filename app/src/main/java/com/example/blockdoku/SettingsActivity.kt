package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 사운드 스위치 설정
        setupSoundSwitch()

        // 홈 버튼 누르면 홈액티비티로 가기
        val btnToHomeSettings = findViewById<Button>(R.id.btnToHomeSettings)
        btnToHomeSettings.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * 사운드 스위치 설정
     */
    private fun setupSoundSwitch() {
        val switchSound = findViewById<SwitchCompat>(R.id.switchSound)

        // SharedPreferences에서 저장된 설정 불러오기
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isSoundOn = prefs.getBoolean("sound_enabled", true)

        // 스위치 상태 설정
        switchSound.isChecked = isSoundOn
        BGMManager.setMuted(!isSoundOn)

        // 스위치 리스너
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            // 설정 저장
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()

            // BGM 음소거 설정
            BGMManager.setMuted(!isChecked)
        }
    }
}