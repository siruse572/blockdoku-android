package com.example.blockdoku

import android.graphics.Color
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 실제 게임 플레이용 9x9 보드 어댑터 (드래그 앤 드롭 인터랙션 지원)
 *
 * 사용 방법:
 * ```kotlin
 * val adapter = GameBoardAdapter(
 *     onCellClick = { position -> /* 셀 클릭 처리 */ },
 *     onBlockPlaced = { startPosition, blockShape -> /* 블록 배치 처리 */ },
 *     onLinesCleared = { clearedLines -> /* 라인 제거 처리 */ },
 *     onScoreUpdate = { score -> /* 점수 업데이트 */ },
 *     onGameOver = { /* 게임 오버 처리 */ }
 * )
 *
 * rvGameBoard.adapter = adapter
 * ```
 */
class GameBoardAdapter(
    private val onCellClick: ((Int) -> Unit)? = null,
    private val onBlockPlaced: ((Int, BlockShape) -> Unit)? = null,
    private val onLinesCleared: ((List<Int>) -> Unit)? = null,
    private val onScoreUpdate: ((Int) -> Unit)? = null,
    private val onGameOver: (() -> Unit)? = null
) : RecyclerView.Adapter<GameBoardAdapter.GameCellViewHolder>() {

    // 게임 보드 상태
    // 0 = 빈 칸, 1 = 채워진 칸, 2 = 폭탄 칸, -1 = 폭발한 칸 (사용 불가)
    private val boardState = IntArray(81) { 0 }

    // 폭탄 타이머 (셀 인덱스 -> 남은 카운트)
    private val bombTimers = mutableMapOf<Int, Int>()

    // 점수
    private var currentScore = 0

    // 배치된 블록 수 (폭탄 생성 카운터)
    private var blocksPlaced = 0

    // 드래그 중인 블록 정보
    private var draggingBlock: BlockShape? = null

    inner class GameCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cellContainer: FrameLayout = itemView.findViewById(R.id.cellContainer)
        val cellFilled: View = itemView.findViewById(R.id.cellFilled)
        val cellBomb: FrameLayout = itemView.findViewById(R.id.cellBomb)
        val txtBombTimer: TextView = itemView.findViewById(R.id.txtBombTimer)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCellClick?.invoke(position)
                }
            }

            itemView.setOnDragListener { view, event ->
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    false
                } else {
                    handleDragEvent(view, event, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_cell, parent, false)

        return GameCellViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameCellViewHolder, position: Int) {
        when (boardState[position]) {
            -1 -> {
                // 폭발한 칸 (사용 불가)
                holder.cellFilled.visibility = View.VISIBLE
                holder.cellFilled.setBackgroundResource(R.drawable.gradient_bomb_exploded)
                holder.cellBomb.visibility = View.GONE
            }

            0 -> {
                // 빈 칸
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.GONE
            }

            1 -> {
                // 채워진 칸
                holder.cellFilled.visibility = View.VISIBLE
                holder.cellFilled.setBackgroundResource(R.drawable.gradient_block_purple)
                holder.cellBomb.visibility = View.GONE
            }

            2 -> {
                // 폭탄 칸
                holder.cellFilled.visibility = View.GONE
                holder.cellBomb.visibility = View.VISIBLE
                holder.txtBombTimer.text = bombTimers[position]?.toString() ?: "0"
            }
        }
    }

    override fun getItemCount(): Int = 81

    /**
     * 드래그 이벤트 처리
     */
    private fun handleDragEvent(view: View, event: DragEvent, position: Int): Boolean {
        return when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                val blockData = event.localState as? BlockShape
                draggingBlock = blockData
                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (canPlaceBlock(position, draggingBlock)) {
                    view.setBackgroundColor(Color.parseColor("#E0E7FF"))
                }
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                view.background = null
                true
            }

            DragEvent.ACTION_DROP -> {
                view.background = null
                draggingBlock?.let { block ->
                    if (canPlaceBlock(position, block)) {
                        placeBlock(position, block)
                    }
                }
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                view.background = null
                draggingBlock = null
                true
            }

            else -> false
        }
    }

    /**
     * 블록을 해당 위치에 놓을 수 있는지 확인
     */
    private fun canPlaceBlock(startPosition: Int, block: BlockShape?): Boolean {
        if (block == null) return false

        val startRow = startPosition / 9
        val startCol = startPosition % 9

        for ((rowOffset, colOffset) in block.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset

            if (row < 0 || row >= 9 || col < 0 || col >= 9) {
                return false
            }

            val pos = row * 9 + col

            if (boardState[pos] != 0) {
                return false
            }
        }

        return true
    }

    /**
     * 블록 배치
     */
    private fun placeBlock(startPosition: Int, block: BlockShape) {
        val startRow = startPosition / 9
        val startCol = startPosition % 9

        for ((rowOffset, colOffset) in block.cells) {
            val row = startRow + rowOffset
            val col = startCol + colOffset
            val pos = row * 9 + col
            boardState[pos] = 1
        }

        blocksPlaced++

        onBlockPlaced?.invoke(startPosition, block)

        decrementBombTimers()

        val clearedPositions = checkAndClearLines()
        if (clearedPositions.isNotEmpty()) {
            onLinesCleared?.invoke(clearedPositions)

            val linesCleared = clearedPositions.size / 9
            currentScore += linesCleared * 100
            onScoreUpdate?.invoke(currentScore)
        }

        if (blocksPlaced % 5 == 0) {
            spawnBomb()
        }

        if (isGameOver()) {
            onGameOver?.invoke()
        }

        notifyDataSetChanged()
    }

    /**
     * 라인 및 3x3 구역 체크 및 제거
     */
    private fun checkAndClearLines(): List<Int> {
        val toClear = mutableSetOf<Int>()

        // 가로 라인 체크
        for (row in 0..8) {
            val isFull = (0..8).all { col ->
                boardState[row * 9 + col] == 1
            }

            if (isFull) {
                for (col in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 세로 라인 체크
        for (col in 0..8) {
            val isFull = (0..8).all { row ->
                boardState[row * 9 + col] == 1
            }

            if (isFull) {
                for (row in 0..8) {
                    toClear.add(row * 9 + col)
                }
            }
        }

        // 3x3 구역 체크
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                var isFull = true

                for (row in 0..2) {
                    for (col in 0..2) {
                        val pos = (boxRow * 3 + row) * 9 + (boxCol * 3 + col)
                        if (boardState[pos] != 1) {
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

        for (pos in toClear) {
            boardState[pos] = 0
            bombTimers.remove(pos)
        }

        return toClear.toList()
    }

    /**
     * 폭탄 타이머 감소 및 폭발 처리
     */
    private fun decrementBombTimers() {
        val toExplode = mutableListOf<Int>()

        for ((pos, timer) in bombTimers.toMap()) {
            val newTimer = timer - 1

            if (newTimer <= 0) {
                toExplode.add(pos)
            } else {
                bombTimers[pos] = newTimer
            }
        }

        for (pos in toExplode) {
            boardState[pos] = -1
            bombTimers.remove(pos)
        }
    }

    /**
     * 빈 칸에 랜덤 폭탄 생성
     */
    private fun spawnBomb() {
        val emptyCells = boardState.indices.filter { boardState[it] == 0 }
        if (emptyCells.isEmpty()) return

        val randomCell = emptyCells.random()
        val randomTimer = (5..10).random()

        boardState[randomCell] = 2
        bombTimers[randomCell] = randomTimer
    }

    /**
     * 게임 오버 체크
     */
    private fun isGameOver(): Boolean {
        return boardState.all { it != 0 }
    }

    /**
     * 보드 리셋
     */
    fun resetBoard() {
        boardState.fill(0)
        bombTimers.clear()
        currentScore = 0
        blocksPlaced = 0
        notifyDataSetChanged()
    }

    /**
     * 현재 점수 가져오기
     */
    fun getCurrentScore(): Int = currentScore

    /**
     * 보드 상태 가져오기
     */
    fun getBoardState(): IntArray = boardState.copyOf()
}