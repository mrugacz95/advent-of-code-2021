fun main() {
    fun part1(input: List<String>): Int {
        return input
            .map { it.toInt() }
            .zipWithNext { f, s -> s > f }
            .count { it }
    }

    fun part2(input: List<String>): Int {
        return input
            .map { it.toInt() }
            .windowed(3, partialWindows = true) { it.sum() }
            .dropLast(2)
            .zipWithNext { f, s -> f < s }
            .count { it }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 5)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
