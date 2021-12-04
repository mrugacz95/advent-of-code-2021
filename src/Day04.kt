class BingoBoard(board: List<String>) {
    private val board = board.map { row -> row.trim().split(" +".toRegex()).map { it.toInt() } }
    private val marked = Array(5) { BooleanArray(5) }

    fun isWinning(): Boolean {
        // horizontal
        for (row in marked) {
            if (row.all { it }) {
                return true
            }
        }
        // vertical
        for (col in 0 until marked.first().size) {
            var allColumn = true
            for (row in marked) {
                if (!row[col]) {
                    allColumn = false
                    break
                }
            }
            if (allColumn) {
                return true
            }
        }
        return false
    }

    fun calcScore(callNumber: Int): Int {
        var unmarkedSum = 0
        for ((rowIdx, row) in board.withIndex()) {
            for ((colIdx, number) in row.withIndex()) {
                if (!marked[rowIdx][colIdx]) {
                    unmarkedSum += number
                }
            }
        }
        return unmarkedSum * callNumber
    }

    fun mark(number: Int): Boolean {
        for ((rowIdx, row) in board.withIndex()) {
            for ((colIdx, boardNumber) in row.withIndex()) {
                if (boardNumber == number) {
                    marked[rowIdx][colIdx] = true
                    return true
                }
            }
        }
        return false
    }
}

fun main() {
    fun parseInput(input: List<String>): Pair<List<Int>, List<BingoBoard>> {
        val numbers = input.first().split(',').map { it.toInt() }
        val boards = mutableListOf<BingoBoard>()
        for (line in 2..input.size step 6) {
            boards.add(BingoBoard(input.subList(line, line + 5)))
        }
        return Pair(numbers, boards)
    }

    fun part1(input: List<String>): Int {
        val (numbers, boards) = parseInput(input)
        for (number in numbers) {
            for (board in boards) {
                board.mark(number)
            }
            for (board in boards) {
                if (board.isWinning()) {
                    return board.calcScore(number)
                }
            }
        }
        error("Game unfinished")
    }

    fun part2(input: List<String>): Int {
        val (numbers, boards) = parseInput(input)
        var boardsLeft = boards
        for (number in numbers) {
            val boardsToKeep = mutableListOf<BingoBoard>()
            for (board in boardsLeft) {
                board.mark(number)
            }
            if (boardsLeft.size == 1 && boardsLeft.first().isWinning()) {
                return boardsLeft.first().calcScore(number)
            }
            for (board in boardsLeft) {
                if (!board.isWinning()) {
                    boardsToKeep.add(board)
                }
            }
            boardsLeft = boardsToKeep
        }
        error("Couldn't find last wining board")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}
