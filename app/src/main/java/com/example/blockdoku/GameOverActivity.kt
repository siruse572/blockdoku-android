package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameOverActivity : ComponentActivity() {
    //게임 점수를 받아오기 위한 변수
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //게임 점수 받아오기
        score = intent.getIntExtra("score", 0)

        enableEdgeToEdge()
        setContentView(R.layout.activity_game_over)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_over)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //받아온 게임 점수 업데이트해서 보여주기
        val txtFinalScore = findViewById<TextView>(R.id.txtFinalScore)
        txtFinalScore.text = score.toString()

        //홈 버튼 누르면 홈액티비티로 가기
        val btnGoHome = findViewById<Button>(R.id.btnGoHome)
        btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // 게임오버 화면 종료
        }

        //다시하기 버튼 누르면 게임액티비티로 가기
        val btnPlayAgain = findViewById<Button>(R.id.btnPlayAgain)
        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish() // 게임오버 화면 종료
        }
    }
}