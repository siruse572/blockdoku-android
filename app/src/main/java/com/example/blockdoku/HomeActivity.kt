package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.example.blockdoku.BGMManager

/**
 * 홈 화면 (메인 메뉴)
 *
 * 기능:
 * - 게임 시작
 * - 튜토리얼 보기
 * - 최고 점수 확인
 * - 설정
 */
class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 버튼 리스너 설정
        setupButtons()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)  //음소거 기능
        val isSoundOn = prefs.getBoolean("sound_enabled", true)
        BGMManager.setMuted(!isSoundOn)

        BGMManager.playBGM(this, R.raw.main_music)

        //SFX초기화
        SFXManager.init(this)
    }

    /**
     * 모든 버튼 리스너 설정
     */
    private fun setupButtons() {
        // 게임 시작 버튼
        findViewById<MaterialButton>(R.id.btnStartGame).setOnClickListener {
            SFXManager.playClick(this)//클릭음 재생
            startGame()
        }

        // 튜토리얼 버튼
        findViewById<MaterialButton>(R.id.btnTutorial).setOnClickListener {
            SFXManager.playClick(this)//클릭음 재생
            showTutorial()
        }

        // 최고 점수 버튼
        findViewById<MaterialButton>(R.id.btnHighScore).setOnClickListener {
            SFXManager.playClick(this)//클릭음 재생
            showHighScores()
        }

        // 설정 버튼
        findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
            SFXManager.playClick(this)//클릭음 재생
            showSettings()
        }
    }

    /**
     * 게임 시작
     */
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    /**
     * 튜토리얼 화면으로 이동
     */
    private fun showTutorial() {
        val intent = Intent(this, tutorialActivity::class.java)
        startActivity(intent)
    }

    /**
     * 최고 점수 화면으로 이동
     */
    private fun showHighScores() {
        val intent = Intent(this, HighScoreActivity::class.java)
        startActivity(intent)
    }

    /**
     * 설정 화면으로 이동
     */
    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}