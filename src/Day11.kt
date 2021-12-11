class Grid(private var energyValues: Array<IntArray>) {
    private val width = energyValues.first().size
    private val height = energyValues.size
    val octopuses = width * height
    var totalFlashes = 0
        private set
    var steps = 0
        private set

    companion object {
        const val FLASH_LEVEL = 10
        const val FLASHED = 11
    }

    private val adjacent =
        listOf(Pair(1, 1), Pair(0, 1), Pair(-1, 1), Pair(-1, 0), Pair(-1, -1), Pair(0, -1), Pair(1, -1), Pair(1, 0))

    fun step(): Int {
        val nextValues = energyValues.copyOf()
        for (y in 0 until height) {
            for (x in 0 until width) {
                nextValues[y][x] += 1
            }
        }
        fun checkFlash(y: Int, x: Int) {
            if (nextValues[y][x] == FLASH_LEVEL) {
                nextValues[y][x] = FLASHED
                for ((ny, nx) in getNeighbours(y, x, width, height, adjacent)) {
                    if (nextValues[ny][nx] < FLASH_LEVEL) {
                        nextValues[ny][nx] += 1
                        checkFlash(ny, nx)
                    }
                }
            }
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                checkFlash(y, x)
            }
        }
        var flashedInStep = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (nextValues[y][x] == FLASHED) {
                    flashedInStep += 1
                    nextValues[y][x] = 0
                }
            }
        }
        steps += 1
        totalFlashes += flashedInStep
        energyValues = nextValues
        return flashedInStep
    }

    fun copy() = Grid(energyValues.map { it.clone() }.toTypedArray())
}

fun main() {
    fun parseInput(input: List<String>): Grid {
        return Grid(input.map { it.map { c -> Character.getNumericValue(c) }.toIntArray() }.toTypedArray())
    }

    fun part1(input: Grid, steps: Int = 100): Int {
        for (step in 0 until steps) {
            input.step()
        }
        return input.totalFlashes
    }

    fun part2(input: Grid): Int {
        do {
            val flashes = input.step()
        } while (flashes != input.octopuses)
        return input.steps
    }

    val dayId = "11"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    val smallTestInput = parseInput(readInput("Day${dayId}_test1"))
    check(part1(smallTestInput, 2) == 9)
    check(part1(testInput.copy()) == 1656)
    check(part2(testInput.copy()) == 195)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input.copy()))
    println(part2(input.copy()))
}
