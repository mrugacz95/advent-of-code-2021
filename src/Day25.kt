typealias SeaBottom = Array<CharArray>

fun SeaBottom.print() {
    println(this.joinToString("\n") { it.joinToString("") } + "\n")
}

private const val EMPTY = '.'
private const val RIGHT = '>'
private const val DOWN = 'v'

fun main() {
    fun parseInput(input: List<String>): SeaBottom {
        return input.map { it.toCharArray() }.toTypedArray()
    }

    fun oneTypeStep(
        bottom: SeaBottom,
        cucumber: Char,
        nextFiled: (y: Int, x: Int, height: Int, width: Int) -> Pair<Int, Int>
    ): Pair<Array<CharArray>, Boolean> {
        val height = bottom.size
        val width = bottom.first().size
        val result = Array(height) { CharArray(width) { EMPTY } }
        var moved = false
        for ((y, row) in bottom.withIndex()) {
            for ((x, cell) in row.withIndex()) {
                when (cell) {
                    EMPTY    -> continue
                    cucumber -> {
                        val (ny, nx) = nextFiled(y, x, height, width)
                        if (bottom[ny][nx] == EMPTY) {
                            result[ny][nx] = cell
                            moved = true
                        } else {
                            result[y][x] = cell
                        }
                    }
                    else     -> {
                        result[y][x] = cell
                    }
                }
            }
        }
        return Pair(result, moved)
    }

    fun step(seaBottom: SeaBottom, log: Boolean = false): Pair<SeaBottom, Boolean> {
        val (after1, moved1) = oneTypeStep(seaBottom, RIGHT) { y, x, _, width -> Pair(y, (x + 1) % width) }
        val (after2, moved2) = oneTypeStep(after1, DOWN) { y, x, height, _ -> Pair((y + 1) % height, x) }
        if (log) seaBottom.print()
        return Pair(after2, moved1 || moved2)
    }

    fun part1(bottom: SeaBottom): Int {
        var state = bottom.map { it.clone() }.toTypedArray()
        var steps = 0
        do {
            val (newState, moved) = step(state)
            steps += 1
            state = newState
        } while (moved)
        return steps
    }

    val dayId = "25"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 58)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
}
