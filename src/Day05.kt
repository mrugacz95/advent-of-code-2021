typealias Point = Pair<Int, Int>
typealias Tunnel = Pair<Point, Point>

fun Tunnel.diagonal(): Boolean {
    return this.first.first != this.second.first && this.first.second != this.second.second
}

fun main() {
    fun parseInput(input: List<String>): List<Tunnel> {
        return input.map { row ->
            val (from, to) = row.split(" -> ").map {
                val (x, y) = it.split(',')
                Point(x.toInt(), y.toInt())
            }
            Tunnel(from, to)
        }
    }

    fun part1(input: List<Tunnel>): Int = input
        .filter { !it.diagonal() }
        .flatMap {
            val points = mutableListOf<Point>()
            for (x in it.first.first toward it.second.first) {
                for (y in it.first.second toward it.second.second) {
                    points.add(Pair(x, y))
                }
            }
            points
        }
        .groupingBy { it }
        .eachCount()
        .filter { it.value >= 2 }
        .size

    fun part2(input: List<Tunnel>): Int = input
        .flatMap {
            val points = mutableListOf<Point>()
            val generationMethod: (IntProgression, IntProgression) -> List<Point> = { xRange, yRange ->
                if (it.diagonal()) {
                    xRange.zip(yRange)
                } else {
                    xRange.cartesianProduct(yRange)
                }
            }
            val tunnelPoints = generationMethod(
                (it.first.first toward it.second.first),
                (it.first.second toward it.second.second)
            )
            for ((x, y) in tunnelPoints) {
                points.add(Pair(x, y))
            }
            points
        }
        .groupingBy { it }
        .eachCount()
        .filter { it.value >= 2 }
        .size

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day05_test"))
    check(part1(testInput) == 5)
    check(part2(testInput) == 12)

    val input = parseInput(readInput("Day05"))
    println(part1(input))
    println(part2(input))
}
