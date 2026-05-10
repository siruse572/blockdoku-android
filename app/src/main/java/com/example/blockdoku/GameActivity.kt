package com.example.blockdoku

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.button.MaterialButton

/**
 * 게임 메인 화면
 *
 * 게임 룰:
 * 1. 9x9 보드에 블록 드래그 앤 드롭
 * 2. 가로/세로/3x3 구역 완성 시 제거
 * 3. 블록 5개 배치마다 폭탄 생성 (타이머 6~10)
 * 4. 블록 배치마다 폭탄 타이머 -1
 * 5. 타이머 0 도달 시 폭발 (사용 불가)
 * 6. 배치 불가능 시 게임 오버
 */
class GameActivity : ComponentActivity() {

    // ==================== 상수 ====================

    private val cellSizeDp = 36   // 보드 칸 크기 (DP)
    private val blockSizeDp = 30  // 블록 크기 (DP, 칸보다 작음)

    // ==================== 게임 상태 ====================

    /**
     * 보드 상태 배열 (9x9 = 81칸)
     * 0 = 빈 칸
     * 1 = 블록이 있는 칸
     * 2 = 폭탄이 있는 칸
     * -1 = 폭발한 칸 (사용 불가)
     */
    private val boardState = IntArray(81) { 0 }

    /**
     * 폭탄 타이머
     * Key: 셀 인덱스 (0~80)
     * Value: 남은 턴 수
     */
    private val bombTimers = mutableMapOf<Int, Int>()

    /**
     * 보드에 배치된 블록/폭탄 View들
     * Key: 셀 인덱스 (0~80)
     * Value: View (블록 또는 폭탄)
     */
    private val blockViews = mutableMapOf<Int, View>()

    /**
     * 현재 점수
     */
    private var currentScore = 0

    /**
     * 배치된 블록 개수 (폭탄 생성 카운터)
     */
    private var blocksPlaced = 0

    // ==================== UI 요소 ====================

    private lateinit var gameBoardContainer: FrameLayout  // 게임 보드 컨테이너
    private lateinit var txtCurrentScore: TextView        // 현재 점수 텍스트
    private lateinit var txtHighScore: TextView           // 최고 점수 텍스트

    /**
     * 드래그 프리뷰 View들 (반투명 그림자)
     */
    private val previewViews = mutableListOf<View>()

    /**
     * 현재 사용 가능한 블록 3개
     * null = 이미 사용됨
     */
    private val availableBlocks = mutableListOf<BlockShape?>()

    /**
     * 블록 컨테이너 3개 (화면 하단)
     */
    private val blockContainers = mutableListOf<FrameLayout>()

    // ==================== 생명주기 ====================

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        // 시스템 바 패딩 설정
        val rootView = findViewById<View>(R.id.game)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SFX 초기화
        SFXManager.init(this)

        // UI 요소 초기화
        initializeUI()

        // 보드 드래그 리스너 설정
        setupBoardDragListener()

        // 초기 블록 3개 생성
        setupInitialBlocks()

        // 버튼 리스너 설정
        setupButtons()
        setupBackNavigationHandler()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)  // 음소거 설정
        val isSoundOn = prefs.getBoolean("sound_enabled", true)
        BGMManager.setMuted(!isSoundOn)
    }

    /**
     * UI 요소 초기화
     */
    private fun initializeUI() {
        gameBoardContainer = findViewById(R.id.gameBoardContainer)
        txtCurrentScore = findViewById(R.id.txtCurrentScore)
        txtHighScore = findViewById(R.id.txtHighScore)

        // 블록 컨테이너 3개 추가
        blockContainers.add(findViewById(R.id.block1Container))
        blockContainers.add(findViewById(R.id.block2Container))
        blockContainers.add(findViewById(R.id.block3Container))
    }

    /**
     * 버튼 리스너 설정
     */
    private fun setupButtons() {
        // 홈 버튼 - 게임 종료 확인창 표시
        findViewById<MaterialButton>(R.id.btnHome).setOnClickListener {
            SFXManager.playClick(this)
            showExitConfirmDialog()
        }

        // 재시작 버튼 - 확인창 표시
        findViewById<MaterialButton>(R.id.btnRestart).setOnClickListener {
            SFXManager.playClick(this)
            showRestartConfirmDialog()
        }
    }

    private fun setupBackNavigationHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmDialog()
            }
        })
    }

    private fun showExitConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("게임 나가기")
            .setMessage("게임을 종료하고 홈 화면으로 돌아가시겠습니까?\n현재 진행 상황은 저장되지 않습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 재시작 확인창
     */
    private fun showRestartConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("게임 다시 시작")
            .setMessage("게임을 다시 시작하시겠습니까?")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                restartGame()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ==================== 보드 드래그 리스너 ====================

    /**
     * 게임 보드에 드래그 리스너 설정
     * 블록을 보드에 드래그 앤 드롭하는 전체 로직
     */
    private fun setupBoardDragListener() {
        gameBoardContainer.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    SFXManager.playClick(this)
                    true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val blockShape = event.localState as? BlockShape
                    if (blockShape != null) {
                        val (row, col) = getCellFromPosition(event.x, event.y)
                        showBlockPreview(row, col, blockShape)
                    }
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    clearBlockPreview()
                    true
                }

                DragEvent.ACTION_DROP -> {
                    clearBlockPreview()

                    val blockShape = event.localState as? BlockShape
                    if (blockShape != null) {
                        val (row, col) = getCellFromPosition(event.x, event.y)

                        if (canPlaceBlock(row, col, blockShape)) {
                            placeBlock(row, col, blockShape)
                            removeUsedBlock(blockShape)

                            blocksPlaced++

                            decrementBombTimers()
                            checkAndClearLines()

                            if (blocksPlaced % 5 == 0) {
                                spawnBomb()
                            }

                            checkGameOver()
                        } else {
                            SFXManager.playError(this)
                        }
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    clearBlockPreview()

                    // 드래그 실패 시 원본 블록 불투명도 복원
                    for (i in blockContainers.indices) {
                        val child = blockContainers[i].getChildAt(0)
                        child?.alpha = 1.0f
                    }

                    true
                }

                else -> false
            }
        }
    }

    // ==================== 블록 배치 ====================

    /**
     * 블록을 해당 위치에 배치할 수 있는지 확인
     */
    private fun canPlaceBlock(startRow: Int, startCol: Int, blockShape: BlockShape): Boolean {
        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset

            if (row < 0 || row >= 9 || col < 0 || col >= 9) {
                return false
            }

            val index = row * 9 + col

            if (boardState[index] != 0) {
                return false
            }
        }
        return true
    }

    /**
     * 블록을 보드에 배치
     */
    private fun placeBlock(startRow: Int, startCol: Int, blockShape: BlockShape) {
        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset
            val index = row * 9 + col

            boardState[index] = 1

            val blockView = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    dp(blockSizeDp).toInt(),
                    dp(blockSizeDp).toInt()
                )
                setBackgroundResource(R.drawable.gradient_block_purple)
            }

            val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
            blockView.x = col * dp(cellSizeDp) + offset
            blockView.y = row * dp(cellSizeDp) + offset

            gameBoardContainer.addView(blockView)
            blockViews[index] = blockView
        }
    }

    // ==================== 블록 프리뷰 ====================

    /**
     * 블록 배치 프리뷰 표시
     */
    private fun showBlockPreview(startRow: Int, startCol: Int, blockShape: BlockShape) {
        clearBlockPreview()

        if (!canPlaceBlock(startRow, startCol, blockShape)) {
            return
        }

        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset

            if (row in 0..8 && col in 0..8) {
                val previewView = View(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        dp(blockSizeDp).toInt(),
                        dp(blockSizeDp).toInt()
                    )
                    setBackgroundColor(Color.parseColor("#80A855F7"))
                }

                val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
                previewView.x = col * dp(cellSizeDp) + offset
                previewView.y = row * dp(cellSizeDp) + offset

                gameBoardContainer.addView(previewView)
                previewViews.add(previewView)
            }
        }
    }

    /**
     * 프리뷰 블록 전부 제거
     */
    private fun clearBlockPreview() {
        previewViews.forEach { gameBoardContainer.removeView(it) }
        previewViews.clear()
    }

    // ==================== 라인 제거 ====================

    /**
     * 완성된 라인/구역 체크 및 제거
     */
    private fun checkAndClearLines() {
        val toClear = mutableSetOf<Int>()
        var lineClearCheck = false

        // 가로 라인 체크
        for (row in 0..8) {
            if ((0..8).all { col -> boardState[row * 9 + col] == 1 || boardState[row * 9 + col] == 2 }) {
                for (col in 0..8) {
                    toClear.add(row * 9 + col)
                    lineClearCheck = true
                }
            }
        }

        // 세로 라인 체크
        for (col in 0..8) {
            if ((0..8).all { row -> boardState[row * 9 + col] == 1 || boardState[row * 9 + col] == 2 }) {
                for (row in 0..8) {
                    toClear.add(row * 9 + col)
                    lineClearCheck = true
                }
            }
        }

        // 3x3 구역 체크
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                var isFull = true

                for (row in 0..2) {
                    for (col in 0..2) {
                        val index = (boxRow * 3 + row) * 9 + (boxCol * 3 + col)
                        if (boardState[index] != 1 && boardState[index] != 2) {
                            isFull = false
                            break
                        }
                    }
                    if (!isFull) break
                }

                if (isFull) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                            toClear.add((boxRow * 3 + row) * 9 + (boxCol * 3 + col))
                            lineClearCheck = true
                        }
                    }
                }
            }
        }

        var blocksCleared = 0
        var bombsCleared = 0
        var bombBonusScore = 0

        if (toClear.isNotEmpty() && lineClearCheck) {
            SFXManager.playLineClear(this)
        }

        for (index in toClear) {
            when (boardState[index]) {
                1 -> {
                    blocksCleared++
                    removeBlock(index)
                }

                2 -> {
                    bombsCleared++
                    bombBonusScore += bombTimers[index] ?: 0
                    removeBomb(index)
                }
            }
        }

        if (toClear.isNotEmpty()) {
            val score = blocksCleared + (bombsCleared * 10) + bombBonusScore
            currentScore += score
            txtCurrentScore.text = currentScore.toString()
        }
    }

    /**
     * 블록 제거
     */
    private fun removeBlock(index: Int) {
        boardState[index] = 0

        val view = blockViews[index]
        if (view != null) {
            gameBoardContainer.removeView(view)
            blockViews.remove(index)
        }
    }

    /**
     * 폭탄 제거
     */
    private fun removeBomb(index: Int) {
        boardState[index] = 0
        bombTimers.remove(index)

        val view = blockViews[index]
        if (view != null) {
            gameBoardContainer.removeView(view)
            blockViews.remove(index)
        }
    }

    // ==================== 폭탄 시스템 ====================

    /**
     * 랜덤 위치에 폭탄 생성
     */
    private fun spawnBomb() {
        val emptyCells = (0..80).filter { boardState[it] == 0 }
        if (emptyCells.isEmpty()) return

        val index = emptyCells.random()
        val row = index / 9
        val col = index % 9
        val timer = (6..10).random()

        placeBomb(row, col, timer)
    }

    /**
     * 폭탄 배치
     */
    private fun placeBomb(row: Int, col: Int, timer: Int) {
        val index = row * 9 + col

        boardState[index] = 2
        bombTimers[index] = timer

        val bombView = layoutInflater.inflate(
            R.layout.view_bomb_block,
            gameBoardContainer,
            false
        ) as FrameLayout

        val txtTimer = bombView.findViewById<TextView>(R.id.txtBombTimer)
        txtTimer?.text = timer.toString()

        updateBombColor(bombView, timer)

        bombView.layoutParams = FrameLayout.LayoutParams(
            dp(blockSizeDp).toInt(),
            dp(blockSizeDp).toInt()
        )

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
        bombView.x = col * dp(cellSizeDp) + offset
        bombView.y = row * dp(cellSizeDp) + offset

        gameBoardContainer.addView(bombView)
        blockViews[index] = bombView
    }

    /**
     * 모든 폭탄 타이머 1씩 감소
     */
    private fun decrementBombTimers() {
        val toExplode = mutableListOf<Int>()

        for ((index, timer) in bombTimers.toMap()) {
            val newTimer = timer - 1

            if (newTimer <= 0) {
                toExplode.add(index)
                SFXManager.playBomb(this)
            } else {
                bombTimers[index] = newTimer

                val view = blockViews[index] as? FrameLayout
                val txtTimer = view?.findViewById<TextView>(R.id.txtBombTimer)
                txtTimer?.text = newTimer.toString()

                if (view != null) {
                    updateBombColor(view, newTimer)
                }
            }
        }

        for (index in toExplode) {
            explodeBomb(index)
        }
    }

    /**
     * 폭탄 색상 업데이트
     */
    private fun updateBombColor(bombView: FrameLayout, timer: Int) {
        val colorRes = when {
            timer >= 7 -> R.drawable.gradient_bomb_safe
            timer >= 4 -> R.drawable.gradient_bomb_warning
            else -> R.drawable.gradient_bomb_danger
        }
        bombView.setBackgroundResource(colorRes)
    }

    /**
     * 폭탄 폭발 처리
     */
    private fun explodeBomb(index: Int) {
        boardState[index] = -1
        bombTimers.remove(index)

        val view = blockViews[index]
        if (view != null) {
            gameBoardContainer.removeView(view)
            blockViews.remove(index)
        }

        val blackView = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dp(blockSizeDp).toInt(),
                dp(blockSizeDp).toInt()
            )
            setBackgroundResource(R.drawable.bg_black_rounded)
        }

        val row = index / 9
        val col = index % 9
        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2

        blackView.x = col * dp(cellSizeDp) + offset
        blackView.y = row * dp(cellSizeDp) + offset

        gameBoardContainer.addView(blackView)
        blockViews[index] = blackView
    }

    // ==================== 초기 블록 생성 ====================

    /**
     * 초기 블록 3개 랜덤 생성
     */
    private fun setupInitialBlocks() {
        val blocks = BlockShape.randomThree()

        availableBlocks.clear()
        availableBlocks.addAll(blocks)

        for (i in 0..2) {
            setupBlockView(blockContainers[i], blocks[i], i)
        }
    }

    /**
     * 블록 View 생성 및 드래그 설정
     */
    private fun setupBlockView(container: FrameLayout, blockShape: BlockShape, index: Int) {
        container.removeAllViews()

        val blockView = createBlockView(blockShape)
        container.addView(blockView)

        blockView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (availableBlocks[index] != null) {
                    val shadow = View.DragShadowBuilder(view)

                    @Suppress("DEPRECATION")
                    view.startDrag(
                        null,
                        shadow,
                        blockShape,
                        0
                    )

                    view.alpha = 0.3f
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * 블록 View 생성 (GridLayout 사용)
     */
    private fun createBlockView(blockShape: BlockShape): View {
        val gridLayout = GridLayout(this)

        val maxRow = blockShape.cells.maxOf { it.first } + 1
        val maxCol = blockShape.cells.maxOf { it.second } + 1

        gridLayout.rowCount = maxRow
        gridLayout.columnCount = maxCol

        // 하단 블록 미리보기 크기 조정
        // 4칸/5칸짜리 블록이 하단 카드에서 잘리지 않도록 큰 블록일수록 작게 표시
        val previewCellSizeDp = when {
            maxRow >= 5 || maxCol >= 5 -> 18
            maxRow >= 4 || maxCol >= 4 -> 20
            else -> 24
        }

        val cellSize = previewCellSizeDp.dpToPx()
        val marginSize = 1.dpToPx()

        gridLayout.layoutParams = FrameLayout.LayoutParams(
            maxCol * cellSize + (maxCol - 1) * marginSize * 2,
            maxRow * cellSize + (maxRow - 1) * marginSize * 2
        )

        for (row in 0 until maxRow) {
            for (col in 0 until maxCol) {
                val cellView = View(this)
                val layoutParams = GridLayout.LayoutParams()

                layoutParams.width = cellSize
                layoutParams.height = cellSize
                layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize)
                layoutParams.rowSpec = GridLayout.spec(row)
                layoutParams.columnSpec = GridLayout.spec(col)

                if (blockShape.cells.contains(row to col)) {
                    cellView.setBackgroundResource(R.drawable.gradient_block_purple)
                } else {
                    cellView.visibility = View.INVISIBLE
                }

                gridLayout.addView(cellView, layoutParams)
            }
        }

        return gridLayout
    }

    /**
     * 사용한 블록 제거
     */
    private fun removeUsedBlock(blockShape: BlockShape) {
        for (i in availableBlocks.indices) {
            if (availableBlocks[i] == blockShape) {
                availableBlocks[i] = null

                val container = blockContainers[i]
                container.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        container.removeAllViews()
                        container.alpha = 1f

                        if (availableBlocks.all { it == null }) {
                            setupInitialBlocks()
                        }

                        checkGameOver()
                    }
                    .start()

                break
            }
        }
    }

    // ==================== 게임 오버 ====================

    /**
     * 게임 오버 체크
     */
    private fun checkGameOver() {
        val remainingBlocks = availableBlocks.filterNotNull()
        if (remainingBlocks.isEmpty()) {
            return
        }

        val hasValidMove = remainingBlocks.any { block ->
            (0..8).any { row ->
                (0..8).any { col ->
                    canPlaceBlock(row, col, block)
                }
            }
        }

        if (!hasValidMove) {
            goToGameOver()
            SFXManager.playClear(this)
        }
    }

    private fun goToGameOver() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", currentScore)
        startActivity(intent)
    }

    /**
     * 게임 재시작
     */
    private fun restartGame() {
        boardState.fill(0)
        bombTimers.clear()
        blockViews.values.forEach { gameBoardContainer.removeView(it) }
        blockViews.clear()

        currentScore = 0
        blocksPlaced = 0
        txtCurrentScore.text = "0"

        setupInitialBlocks()
    }

    // ==================== 유틸리티 함수 ====================

    /**
     * 화면 좌표를 셀 좌표로 변환
     */
    private fun getCellFromPosition(x: Float, y: Float): Pair<Int, Int> {
        val cellSize = dp(cellSizeDp)
        val col = (x / cellSize).toInt().coerceIn(0, 8)
        val row = (y / cellSize).toInt().coerceIn(0, 8)
        return row to col
    }

    /**
     * DP를 픽셀로 변환
     */
    private fun dp(value: Int): Float {
        return value * resources.displayMetrics.density
    }

    /**
     * DP를 픽셀로 변환 (Int 반환)
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        BGMManager.playBGM(this, R.raw.game_music)
    }

    override fun onPause() {
        super.onPause()
        BGMManager.playBGM(this, R.raw.main_music)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}