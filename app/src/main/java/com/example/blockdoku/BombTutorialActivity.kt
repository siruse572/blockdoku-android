package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

/**
 * 폭탄 시스템 튜토리얼
 *
 * 기능:
 * - 폭탄 시스템 설명
 * - 안전/주의/위험 상태 표시
 * - 시작하기 → 게임 시작
 */
class BombTutorialActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bomb_tutorial)

        //SFX초기화
        SFXManager.init(this)

        // 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bomb_tutorial)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 시작 버튼 리스너
        findViewById<MaterialButton>(R.id.btnStart).setOnClickListener {
            SFXManager.playClick(this)//클릭음 재생
            startGame()
        }
    }

    /**
     * 게임 시작
     */
    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()  // 튜토리얼 종료
    }
}