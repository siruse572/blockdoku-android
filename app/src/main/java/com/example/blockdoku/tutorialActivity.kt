package com.example.blockdoku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

/**
 * 튜토리얼 화면
 *
 * 기능:
 * - 게임 방법 설명
 * - 샘플 보드 표시
 * - 건너뛰기 → 홈으로
 * - 다음 → 폭탄 튜토리얼 or 게임 시작
 */
class tutorialActivity : ComponentActivity() {

    private val cellSizeDp = 36   // 칸 크기
    private val blockSizeDp = 30  // 블록 크기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tutorial)

        // 시스템 바 패딩 설정
        val rootView = findViewById<View>(R.id.tutorial)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 보드 컨테이너
        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        // 샘플 블록 배치
        setupSampleBoard(boardContainer)

        // 버튼 리스너 설정
        setupButtons()

        // SFX 초기화
        SFXManager.init(this)
    }

    /**
     * 샘플 보드에 블록 배치
     */
    private fun setupSampleBoard(boardContainer: FrameLayout) {
        // L자 블록 (보라색)
        placeBlock(boardContainer, 0, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 1, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 2, 0, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 2, 1, R.drawable.gradient_block_purple)

        // 정사각형 블록 (보라색)
        placeBlock(boardContainer, 3, 4, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 3, 5, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 4, 4, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 4, 5, R.drawable.gradient_block_purple)

        // 작은 블록 (보라색)
        placeBlock(boardContainer, 7, 7, R.drawable.gradient_block_purple)
        placeBlock(boardContainer, 7, 8, R.drawable.gradient_block_purple)

        // 폭탄 2개
        placeBomb(boardContainer, 6, 4, "7")
        placeBomb(boardContainer, 7, 1, "3")
    }

    /**
     * 블록 배치
     */
    private fun placeBlock(
        container: FrameLayout,
        row: Int,
        col: Int,
        drawableRes: Int
    ) {
        val block = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dp(blockSizeDp).toInt(),
                dp(blockSizeDp).toInt()
            )
            background = getDrawable(drawableRes)
        }

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2

        block.x = col * dp(cellSizeDp) + offset
        block.y = row * dp(cellSizeDp) + offset

        container.addView(block)
    }

    /**
     * 폭탄 배치
     */
    private fun placeBomb(
        container: FrameLayout,
        row: Int,
        col: Int,
        timer: String
    ) {
        val bombView = layoutInflater.inflate(
            R.layout.view_bomb_block,
            container,
            false
        )

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2

        bombView.x = col * dp(cellSizeDp) + offset
        bombView.y = row * dp(cellSizeDp) + offset

        container.addView(bombView)
    }

    /**
     * 버튼 리스너 설정
     */
    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnSkip).setOnClickListener {
            SFXManager.playClick(this)
            goToHome()
        }

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            SFXManager.playClick(this)
            goToBombTutorial()
        }
    }

    /**
     * 홈 화면으로 이동
     */
    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 폭탄 튜토리얼로 이동
     */
    private fun goToBombTutorial() {
        val intent = Intent(this, BombTutorialActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * DP를 픽셀로 변환
     */
    private fun dp(value: Int): Float =
        value * resources.displayMetrics.density
}