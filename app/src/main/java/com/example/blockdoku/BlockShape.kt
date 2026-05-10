package com.example.blockdoku

/**
 * 블록 형태 데이터 클래스
 *
 * 블록은 (행 오프셋, 열 오프셋) 좌표들의 리스트로 정의됩니다.
 * 예: 2x2 정사각형 블록 = [(0,0), (0,1), (1,0), (1,1)]
 */
data class BlockShape(
    val cells: List<Pair<Int, Int>>, // (행 오프셋, 열 오프셋) 리스트
    val color: String = "#9333EA" // 기본 보라색 (purple-600)
) {
    companion object {
        // ==================== 기본 블록 형태 ====================

        /**
         * 1x1 단일 블록
         */
        val SINGLE = BlockShape(listOf(0 to 0))

        /**
         * 1x2 가로 블록
         */
        val HORIZONTAL_2 = BlockShape(listOf(0 to 0, 0 to 1))

        /**
         * 2x1 세로 블록
         */
        val VERTICAL_2 = BlockShape(listOf(0 to 0, 1 to 0))

        /**
         * 1x3 가로 블록
         */
        val HORIZONTAL_3 = BlockShape(listOf(0 to 0, 0 to 1, 0 to 2))

        /**
         * 3x1 세로 블록
         */
        val VERTICAL_3 = BlockShape(listOf(0 to 0, 1 to 0, 2 to 0))

        /**
         * 1x4 가로 블록
         */
        val HORIZONTAL_4 = BlockShape(listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3))

        /**
         * 4x1 세로 블록
         */
        val VERTICAL_4 = BlockShape(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0))

        /**
         * 1x5 가로 블록
         */
        val HORIZONTAL_5 = BlockShape(listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4))

        /**
         * 5x1 세로 블록
         */
        val VERTICAL_5 = BlockShape(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0))

        // ==================== 정사각형 블록 ====================

        /**
         * 2x2 정사각형 블록
         * ■■
         * ■■
         */
        val SQUARE_2 = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 0, 1 to 1
        ))

        // ==================== L자 블록 (4가지 회전) ====================

        /**
         * L자 블록 (기본)
         */
        val L_SHAPE = BlockShape(listOf(
            0 to 0,
            1 to 0,
            2 to 0, 2 to 1
        ))

        /**
         * L자 블록 (90도 회전)
         */
        val L_SHAPE_90 = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 0
        ))

        /**
         * L자 블록 (180도 회전)
         */
        val L_SHAPE_180 = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 1,
            2 to 1
        ))

        /**
         * L자 블록 (270도 회전)
         */
        val L_SHAPE_270 = BlockShape(listOf(
            0 to 2,
            1 to 0, 1 to 1, 1 to 2
        ))

        // ==================== 역 L자 블록 (4가지 회전) ====================

        /**
         * 역 L자 블록 (기본)
         */
        val L_REVERSE = BlockShape(listOf(
            0 to 1,
            1 to 1,
            2 to 0, 2 to 1
        ))

        /**
         * 역 L자 블록 (90도 회전)
         */
        val L_REVERSE_90 = BlockShape(listOf(
            0 to 0,
            1 to 0, 1 to 1, 1 to 2
        ))

        /**
         * 역 L자 블록 (180도 회전)
         */
        val L_REVERSE_180 = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 0,
            2 to 0
        ))

        /**
         * 역 L자 블록 (270도 회전)
         */
        val L_REVERSE_270 = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 2
        ))

        // ==================== T자 블록 (4가지 회전) ====================

        /**
         * T자 블록 (기본)
         */
        val T_SHAPE = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 1
        ))

        /**
         * T자 블록 (90도 회전)
         */
        val T_SHAPE_90 = BlockShape(listOf(
            0 to 1,
            1 to 0, 1 to 1,
            2 to 1
        ))

        /**
         * T자 블록 (180도 회전)
         */
        val T_SHAPE_180 = BlockShape(listOf(
            0 to 1,
            1 to 0, 1 to 1, 1 to 2
        ))

        /**
         * T자 블록 (270도 회전)
         */
        val T_SHAPE_270 = BlockShape(listOf(
            0 to 0,
            1 to 0, 1 to 1,
            2 to 0
        ))

        // ==================== Z자 블록 (2가지 회전) ====================

        /**
         * Z자 블록 (가로)
         */
        val Z_SHAPE = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 1, 1 to 2
        ))

        /**
         * Z자 블록 (세로)
         */
        val Z_SHAPE_90 = BlockShape(listOf(
            0 to 1,
            1 to 0, 1 to 1,
            2 to 0
        ))

        // ==================== 역 Z자 블록 (2가지 회전) ====================

        /**
         * 역 Z자 블록 (가로)
         */
        val S_SHAPE = BlockShape(listOf(
            0 to 1, 0 to 2,
            1 to 0, 1 to 1
        ))

        /**
         * 역 Z자 블록 (세로)
         */
        val S_SHAPE_90 = BlockShape(listOf(
            0 to 0,
            1 to 0, 1 to 1,
            2 to 1
        ))

        // ==================== 코너 블록 ====================

        /**
         * 작은 코너 블록
         */
        val CORNER_SMALL = BlockShape(listOf(
            0 to 0, 0 to 1,
            1 to 0
        ))

        /**
         * 큰 코너 블록
         */
        val CORNER_LARGE = BlockShape(listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 0,
            2 to 0
        ))

        // ==================== 십자가 블록 ====================

        /**
         * 작은 십자가
         */
        val CROSS_SMALL = BlockShape(listOf(
            0 to 1,
            1 to 0, 1 to 1, 1 to 2,
            2 to 1
        ))

        /**
         * 큰 십자가

         */
        val CROSS_LARGE = BlockShape(listOf(
            0 to 2,
            1 to 2,
            2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4,
            3 to 2,
            4 to 2
        ))

        // ==================== 유틸리티 메서드 ====================

        /**
         * 모든 사용 가능한 블록 형태 리스트
         */
        val ALL_SHAPES = listOf(
            SINGLE,
            HORIZONTAL_2, VERTICAL_2,
            HORIZONTAL_3, VERTICAL_3,
            HORIZONTAL_4, VERTICAL_4,
            HORIZONTAL_5, VERTICAL_5,
            SQUARE_2,
            L_SHAPE, L_SHAPE_90, L_SHAPE_180, L_SHAPE_270,
            L_REVERSE, L_REVERSE_90, L_REVERSE_180, L_REVERSE_270,
            T_SHAPE, T_SHAPE_90, T_SHAPE_180, T_SHAPE_270,
            Z_SHAPE, Z_SHAPE_90,
            S_SHAPE, S_SHAPE_90,
            CORNER_SMALL, CORNER_LARGE,
            CROSS_SMALL
        )

        /**
         * 기본 난이도 블록만 (작은 블록들)
         */
        val EASY_SHAPES = listOf(
            SINGLE,
            HORIZONTAL_2, VERTICAL_2,
            HORIZONTAL_3, VERTICAL_3,
            SQUARE_2,
            CORNER_SMALL
        )

        /**
         * 중간 난이도 블록
         */
        val MEDIUM_SHAPES = listOf(
            HORIZONTAL_3, VERTICAL_3,
            HORIZONTAL_4, VERTICAL_4,
            SQUARE_2,
            L_SHAPE, L_SHAPE_90,
            T_SHAPE, T_SHAPE_90,
            Z_SHAPE, S_SHAPE
        )

        /**
         * 높은 난이도 블록
         */
        val HARD_SHAPES = listOf(
            HORIZONTAL_4, VERTICAL_4,
            HORIZONTAL_5, VERTICAL_5,
            L_SHAPE, L_SHAPE_90, L_SHAPE_180, L_SHAPE_270,
            L_REVERSE, L_REVERSE_90, L_REVERSE_180, L_REVERSE_270,
            T_SHAPE, T_SHAPE_90, T_SHAPE_180, T_SHAPE_270,
            Z_SHAPE, Z_SHAPE_90,
            S_SHAPE, S_SHAPE_90,
            CORNER_LARGE,
            CROSS_SMALL
        )

        /**
         * 랜덤 블록 생성
         */
        fun random(): BlockShape {
            return ALL_SHAPES.random()
        }

        /**
         * 난이도별 랜덤 블록 생성
         */
        fun randomByDifficulty(difficulty: Difficulty = Difficulty.MEDIUM): BlockShape {
            return when (difficulty) {
                Difficulty.EASY -> EASY_SHAPES.random()
                Difficulty.MEDIUM -> MEDIUM_SHAPES.random()
                Difficulty.HARD -> HARD_SHAPES.random()
            }
        }

        /**
         * 3개의 랜덤 블록 생성
         */
        fun randomThree(difficulty: Difficulty = Difficulty.MEDIUM): List<BlockShape> {
            return List(3) { randomByDifficulty(difficulty) }
        }

        /**
         * 특정 크기 이하의 랜덤 블록 생성
         */
        fun randomWithMaxSize(maxCells: Int): BlockShape {
            val filtered = ALL_SHAPES.filter { it.cells.size <= maxCells }
            return filtered.random()
        }
    }

    /**
     * 블록이 차지하는 총 셀 개수
     */
    val size: Int
        get() = cells.size

    /**
     * 블록의 경계 상자 크기 (행, 열)
     */
    val boundingBox: Pair<Int, Int>
        get() {
            val maxRow = cells.maxOf { it.first } + 1
            val maxCol = cells.maxOf { it.second } + 1
            return maxRow to maxCol
        }

    /**
     * 블록을 90도 회전
     */
    fun rotate90(): BlockShape {
        val rotatedCells = cells.map { (row, col) ->
            col to -row
        }
        // 좌표를 (0,0) 기준으로 정규화
        val minRow = rotatedCells.minOf { it.first }
        val minCol = rotatedCells.minOf { it.second }
        val normalizedCells = rotatedCells.map { (row, col) ->
            (row - minRow) to (col - minCol)
        }
        return BlockShape(normalizedCells, color)
    }

    /**
     * 블록을 180도 회전
     */
    fun rotate180(): BlockShape {
        return rotate90().rotate90()
    }

    /**
     * 블록을 270도 회전
     */
    fun rotate270(): BlockShape {
        return rotate90().rotate90().rotate90()
    }

    /**
     * 블록을 좌우 반전
     */
    fun flipHorizontal(): BlockShape {
        val flippedCells = cells.map { (row, col) ->
            row to -col
        }
        val minCol = flippedCells.minOf { it.second }
        val normalizedCells = flippedCells.map { (row, col) ->
            row to (col - minCol)
        }
        return BlockShape(normalizedCells, color)
    }

    /**
     * 블록을 상하 반전
     */
    fun flipVertical(): BlockShape {
        val flippedCells = cells.map { (row, col) ->
            -row to col
        }
        val minRow = flippedCells.minOf { it.first }
        val normalizedCells = flippedCells.map { (row, col) ->
            (row - minRow) to col
        }
        return BlockShape(normalizedCells, color)
    }
}

/**
 * 게임 난이도
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}
