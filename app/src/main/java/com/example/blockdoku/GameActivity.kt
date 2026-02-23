package com.example.blockdoku

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import com.yourpackage.blockdoku.model.BlockShape

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI 요소 초기화
        initializeUI()

        // 보드 드래그 리스너 설정
        setupBoardDragListener()

        // 초기 블록 3개 생성
        setupInitialBlocks()

        // 버튼 리스너 설정
        setupButtons()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)  //음소거 설정
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
        // 홈 버튼 - 게임 종료
        findViewById<MaterialButton>(R.id.btnHome).setOnClickListener {
            finish()
        }

        // 재시작 버튼 - 게임 리셋
        findViewById<MaterialButton>(R.id.btnRestart).setOnClickListener {
            restartGame()
        }
    }

    // ==================== 보드 드래그 리스너 ====================

    /**
     * 게임 보드에 드래그 리스너 설정
     * 블록을 보드에 드래그 앤 드롭하는 전체 로직
     */
    private fun setupBoardDragListener() {
        gameBoardContainer.setOnDragListener { _, event ->
            when (event.action) {
                // 드래그 시작
                DragEvent.ACTION_DRAG_STARTED -> {
                    true
                }

                // 보드 위로 진입
                DragEvent.ACTION_DRAG_ENTERED -> {
                    true
                }

                // 드래그 중 (마우스 이동)
                DragEvent.ACTION_DRAG_LOCATION -> {
                    // 드래그 중인 블록 가져오기
                    val blockShape = event.localState as? BlockShape
                    if (blockShape != null) {
                        // 현재 마우스 위치의 셀 계산
                        val (row, col) = getCellFromPosition(event.x, event.y)

                        // 프리뷰 표시 (반투명 그림자)
                        showBlockPreview(row, col, blockShape)
                    }
                    true
                }

                // 보드 밖으로 나감
                DragEvent.ACTION_DRAG_EXITED -> {
                    // 프리뷰 제거
                    clearBlockPreview()
                    true
                }

                // 드롭 (블록 배치)
                DragEvent.ACTION_DROP -> {
                    // 프리뷰 제거
                    clearBlockPreview()

                    val blockShape = event.localState as? BlockShape
                    if (blockShape != null) {
                        // 드롭 위치의 셀 계산
                        val (row, col) = getCellFromPosition(event.x, event.y)

                        // 배치 가능한지 확인
                        if (canPlaceBlock(row, col, blockShape)) {
                            // 블록 배치
                            placeBlock(row, col, blockShape)

                            // 사용한 블록 제거
                            removeUsedBlock(blockShape)

                            // 블록 배치 카운터 증가
                            blocksPlaced++

                            // 모든 폭탄 타이머 감소
                            decrementBombTimers()

                            // 라인/구역 체크 및 제거
                            checkAndClearLines()

                            // 5개마다 폭탄 생성
                            if (blocksPlaced % 5 == 0) {
                                spawnBomb()
                            }

                            // 게임 오버 체크
                            checkGameOver()
                        }
                    }
                    true
                }

                // 드래그 종료
                DragEvent.ACTION_DRAG_ENDED -> {
                    clearBlockPreview()

                    //드래그 실패 시 원본 블록 불투명도 복원
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
     *
     * @param startRow 시작 행 (0~8)
     * @param startCol 시작 열 (0~8)
     * @param blockShape 배치할 블록
     * @return 배치 가능 여부
     */
    private fun canPlaceBlock(startRow: Int, startCol: Int, blockShape: BlockShape): Boolean {
        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset

            // 범위 체크 (보드 밖인지)
            if (row < 0 || row >= 9 || col < 0 || col >= 9) {
                return false
            }

            val index = row * 9 + col

            // 이미 블록이 있거나 폭발한 칸인지 체크
            if (boardState[index] != 0) {
                return false
            }
        }
        return true
    }

    /**
     * 블록을 보드에 배치
     *
     * @param startRow 시작 행
     * @param startCol 시작 열
     * @param blockShape 배치할 블록
     */
    private fun placeBlock(startRow: Int, startCol: Int, blockShape: BlockShape) {
        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset
            val index = row * 9 + col

            // 보드 상태 업데이트
            boardState[index] = 1

            // 블록 View 생성
            val blockView = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    dp(blockSizeDp).toInt(),
                    dp(blockSizeDp).toInt()
                )
                setBackgroundResource(R.drawable.gradient_block_purple)
            }

            // 칸 중앙에 배치하기 위한 오프셋 계산
            val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
            blockView.x = col * dp(cellSizeDp) + offset
            blockView.y = row * dp(cellSizeDp) + offset

            // 보드에 추가
            gameBoardContainer.addView(blockView)
            blockViews[index] = blockView
        }
    }

    // ==================== 블록 프리뷰 (드래그 중 그림자) ====================

    /**
     * 블록 배치 프리뷰 표시 (반투명 그림자)
     *
     * @param startRow 시작 행
     * @param startCol 시작 열
     * @param blockShape 배치할 블록
     */
    private fun showBlockPreview(startRow: Int, startCol: Int, blockShape: BlockShape) {
        // 기존 프리뷰 제거
        clearBlockPreview()

        // 배치 불가능하면 프리뷰 표시 안 함
        if (!canPlaceBlock(startRow, startCol, blockShape)) {
            return
        }

        // 프리뷰 블록 생성 (반투명 보라색)
        for ((rowOffset, colOffset) in blockShape.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset

            if (row in 0..8 && col in 0..8) {
                val previewView = View(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        dp(blockSizeDp).toInt(),
                        dp(blockSizeDp).toInt()
                    )
                    // 반투명 보라색 (80은 투명도 50%)
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
     * - 가로 라인 (9칸)
     * - 세로 라인 (9칸)
     * - 3x3 구역 (9칸)
     */
    private fun checkAndClearLines() {
        val toClear = mutableSetOf<Int>()

        // 가로 라인 체크 (9개)
        for (row in 0..8) {
            if ((0..8).all { col -> boardState[row * 9 + col] == 1 || boardState[row * 9 + col] == 2}) {
                for (col in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 세로 라인 체크 (9개)
        for (col in 0..8) {
            if ((0..8).all { row -> boardState[row * 9 + col] == 1 || boardState[row * 9 + col] == 2}) {
                for (row in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 3x3 구역 체크 (9개)
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
                        }
                    }
                }
            }
        }

        // 제거 및 점수 계산
        var blocksCleared = 0      // 제거된 블록 개수
        var bombsCleared = 0       // 제거된 폭탄 개수
        var bombBonusScore = 0     // 폭탄 타이머 보너스 점수

        for (index in toClear) {
            when (boardState[index]) {
                1 -> {
                    // 일반 블록 제거
                    blocksCleared++
                    removeBlock(index)
                }
                2 -> {
                    // 폭탄 제거 (타이머 보너스 점수 추가)
                    bombsCleared++
                    bombBonusScore += bombTimers[index] ?: 0
                    removeBomb(index)
                }
            }
        }

        // 점수 업데이트
        // 점수 = 블록 개수 + (폭탄 개수 × 10) + 폭탄 타이머 합계
        if (toClear.isNotEmpty()) {
            val score = blocksCleared + (bombsCleared * 10) + bombBonusScore
            currentScore += score
            txtCurrentScore.text = currentScore.toString()
        }
    }

    /**
     * 블록 제거
     *
     * @param index 셀 인덱스 (0~80)
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
     *
     * @param index 셀 인덱스 (0~80)
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
     * 타이머: 6~10 랜덤
     */
    private fun spawnBomb() {
        // 빈 칸 찾기
        val emptyCells = (0..80).filter { boardState[it] == 0 }
        if (emptyCells.isEmpty()) return

        // 랜덤 위치 선택
        val index = emptyCells.random()
        val row = index / 9
        val col = index % 9

        // 랜덤 타이머 (6~10)
        val timer = (6..10).random()

        // 폭탄 배치
        placeBomb(row, col, timer)
    }

    /**
     * 폭탄 배치
     *
     * @param row 행 (0~8)
     * @param col 열 (0~8)
     * @param timer 초기 타이머 값
     */
    private fun placeBomb(row: Int, col: Int, timer: Int) {
        val index = row * 9 + col

        // 보드 상태 업데이트
        boardState[index] = 2
        bombTimers[index] = timer

        // 폭탄 View 생성 (view_bomb_block.xml 사용)
        val bombView = layoutInflater.inflate(
            R.layout.view_bomb_block,
            gameBoardContainer,
            false
        ) as FrameLayout

        // 타이머 텍스트 설정
        val txtTimer = bombView.findViewById<TextView>(R.id.txtBombTimer)
        txtTimer?.text = timer.toString()

        // 폭탄 색상 설정 (타이머에 따라)
        updateBombColor(bombView, timer)

        // 크기 및 위치 설정
        bombView.layoutParams = FrameLayout.LayoutParams(
            dp(blockSizeDp).toInt(),
            dp(blockSizeDp).toInt()
        )

        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
        bombView.x = col * dp(cellSizeDp) + offset
        bombView.y = row * dp(cellSizeDp) + offset

        // 보드에 추가
        gameBoardContainer.addView(bombView)
        blockViews[index] = bombView
    }

    /**
     * 모든 폭탄 타이머 1씩 감소
     * 타이머가 0이 되면 폭발
     */
    private fun decrementBombTimers() {
        val toExplode = mutableListOf<Int>()

        for ((index, timer) in bombTimers.toMap()) {
            val newTimer = timer - 1

            if (newTimer <= 0) {
                // 타이머 0 → 폭발
                toExplode.add(index)
            } else {
                // 타이머 업데이트
                bombTimers[index] = newTimer

                // UI 업데이트
                val view = blockViews[index] as? FrameLayout
                val txtTimer = view?.findViewById<TextView>(R.id.txtBombTimer)
                txtTimer?.text = newTimer.toString()

                // 색상 업데이트
                if (view != null) {
                    updateBombColor(view, newTimer)
                }
            }
        }

        // 폭탄 폭발 처리
        for (index in toExplode) {
            explodeBomb(index)
        }
    }

    /**
     * 폭탄 색상 업데이트 (타이머에 따라)
     *
     * @param bombView 폭탄 View
     * @param timer 남은 타이머
     */
    private fun updateBombColor(bombView: FrameLayout, timer: Int) {
        val colorRes = when {
            timer >= 7 -> R.drawable.gradient_bomb_safe    // 초록 (안전)
            timer >= 4 -> R.drawable.gradient_bomb_warning // 노랑 (주의)
            else -> R.drawable.gradient_bomb_danger        // 빨강 (위험)
        }
        bombView.setBackgroundResource(colorRes)
    }

    /**
     * 폭탄 폭발 처리
     * 해당 칸을 사용 불가 상태로 변경
     *
     * @param index 셀 인덱스 (0~80)
     */
    private fun explodeBomb(index: Int) {
        boardState[index] = -1  // 사용 불가 상태
        bombTimers.remove(index)

        val view = blockViews[index]
        if (view != null) {
            // 폭발 효과 (빨간색 X 표시)
            // 기존 폭탄 View 제거
            gameBoardContainer.removeView(view)
            blockViews.remove(index)
        }
        val blackView = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dp(blockSizeDp).toInt(),
                dp(blockSizeDp).toInt()
            )
            setBackgroundResource(R.drawable.bg_black_rounded)  //파괴된 폭탄장소는 검정색 처리
        }
        val row = index / 9
        val col = index % 9
        val offset = (dp(cellSizeDp) - dp(blockSizeDp)) / 2
        blackView.x = col * dp(cellSizeDp) + offset
        blackView.y = row * dp(cellSizeDp) + offset

        // 보드에 추가
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
     *
     * @param container 블록을 담을 컨테이너
     * @param blockShape 블록 모양
     * @param index 블록 인덱스 (0~2)
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
                        null,          // ClipData
                        shadow,        // DragShadow
                        blockShape,    // localState
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
     *
     * @param blockShape 블록 모양
     * @return 생성된 블록 View
     */
    private fun createBlockView(blockShape: BlockShape): View {
        val gridLayout = GridLayout(this)

        // 블록의 최대 행/열 계산
        val maxRow = blockShape.cells.maxOf { it.first } + 1
        val maxCol = blockShape.cells.maxOf { it.second } + 1

        gridLayout.rowCount = maxRow
        gridLayout.columnCount = maxCol

        //GridLayout 크기 명시 (터치 영역 확보)
        val cellSize = 32.dpToPx()
        gridLayout.layoutParams = FrameLayout.LayoutParams(
            maxCol * cellSize + (maxCol - 1) * 4,  // 셀 크기 + 마진
            maxRow * cellSize + (maxRow - 1) * 4
        )

        // 블록 셀 생성
        for (row in 0 until maxRow) {
            for (col in 0 until maxCol) {
                val cellView = View(this)
                val layoutParams = GridLayout.LayoutParams()
                layoutParams.width = cellSize
                layoutParams.height = cellSize
                layoutParams.setMargins(2, 2, 2, 2)
                layoutParams.rowSpec = GridLayout.spec(row)
                layoutParams.columnSpec = GridLayout.spec(col)

                if (blockShape.cells.contains(row to col)) {
                    // 블록 셀 - 보라색
                    cellView.setBackgroundResource(R.drawable.gradient_block_purple)
                } else {
                    // 빈 셀 - 투명
                    cellView.visibility = View.INVISIBLE
                }

                gridLayout.addView(cellView, layoutParams)
            }
        }

        return gridLayout
    }

    /**
     * 사용한 블록 제거 (애니메이션 포함)
     *
     * @param blockShape 제거할 블록
     */
    private fun removeUsedBlock(blockShape: BlockShape) {
        for (i in availableBlocks.indices) {
            if (availableBlocks[i] == blockShape) {
                availableBlocks[i] = null

                // 애니메이션 추가
                val container = blockContainers[i]
                container.animate()
                    .alpha(0f)                // 투명하게
                    .setDuration(200)         // 200ms 동안
                    .withEndAction {
                        container.removeAllViews()
                        container.alpha = 1f  // 다음 블록 위해 불투명도 복원
                        // ✅ 여기서 체크
                        // 3개 블록 모두 사용 시 새 블록 생성
                        if (availableBlocks.all { it == null }) {
                            setupInitialBlocks()
                        }
                        // 모든 상태가 확정된 뒤 게임 오버 체크
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
     * 남은 블록으로 배치 가능한 곳이 있는지 확인
     */
    private fun checkGameOver() {
        // 남은 블록이 있는지 확인
        val remainingBlocks = availableBlocks.filterNotNull()
        if (remainingBlocks.isEmpty()) {
            return  // 블록이 없으면 체크 안 함
        }

        // 각 블록이 배치 가능한 위치가 있는지 확인
        val hasValidMove = remainingBlocks.any { block ->
            (0..8).any { row ->
                (0..8).any { col ->
                    canPlaceBlock(row, col, block)
                }
            }
        }

        // 배치 가능한 곳이 없으면 게임 오버
        if (!hasValidMove) {
            goToGameOver()
        }
    }

    private fun goToGameOver() {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", currentScore)
        startActivity(intent)
        //finish()
    }

    /**
     * 게임 재시작
     * 모든 상태 초기화
     */
    private fun restartGame() {
        // 보드 초기화
        boardState.fill(0)
        bombTimers.clear()
        blockViews.values.forEach { gameBoardContainer.removeView(it) }
        blockViews.clear()

        // 게임 상태 초기화
        currentScore = 0
        blocksPlaced = 0
        txtCurrentScore.text = "0"

        // 새 블록 생성
        setupInitialBlocks()
    }

    // ==================== 유틸리티 함수 ====================

    /**
     * 화면 좌표를 셀 좌표로 변환
     *
     * @param x X 좌표 (픽셀)
     * @param y Y 좌표 (픽셀)
     * @return (행, 열) 쌍
     */
    private fun getCellFromPosition(x: Float, y: Float): Pair<Int, Int> {
        val cellSize = dp(cellSizeDp)
        val col = (x / cellSize).toInt().coerceIn(0, 8)
        val row = (y / cellSize).toInt().coerceIn(0, 8)
        return row to col
    }

    /**
     * DP를 픽셀로 변환
     *
     * @param value DP 값
     * @return 픽셀 값 (Float)
     */
    private fun dp(value: Int): Float {
        return value * resources.displayMetrics.density
    }

    /**
     * DP를 픽셀로 변환 (Int 반환)
     *
     * @receiver DP 값
     * @return 픽셀 값 (Int)
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