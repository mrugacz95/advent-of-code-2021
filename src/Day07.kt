import kotlin.math.abs

fun main() {
    fun parseInput(input: List<String>): List<Int> {
        return input.first().split(',').map { it.toInt() }
    }

    fun findFormationCost(crabsPositions: List<Int>, fuelCost: (pos: Int, dest: Int) -> (Int)): Int {
        val maxPos = crabsPositions.maxOf { it }
        val minPos = crabsPositions.minOf { it }
        return (minPos..maxPos)
            .map { pos -> crabsPositions.sumOf { fuelCost(pos, it) } }
            .minOf { it }
    }

    fun part1(input: List<Int>): Int {
        return findFormationCost(input) { pos, dest -> abs(pos - dest) }
    }

    fun part2(input: List<Int>): Int {
        return findFormationCost(input) { pos, dest ->
            (1..abs(pos - dest)).sum()
        }
    }

    val dayId = "07"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 37)
    check(part2(testInput) == 168)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
