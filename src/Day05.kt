typealias Tunnel = Pair<Point, Point>

private fun Tunnel.diagonal(): Boolean {
    return this.first.y != this.second.y && this.first.x != this.second.x
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
            for (x in it.first.x toward it.second.x) {
                for (y in it.first.y toward it.second.y) {
                    points.add(Point(x = x, y = y))
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
                    xRange.zip(yRange).map { p -> p.toPoint() }
                } else {
                    xRange.cartesianProduct(yRange).map { p -> p.toPoint() }
                }
            }
            val tunnelPoints = generationMethod(
                (it.first.x toward it.second.x),
                (it.first.y toward it.second.y)
            )
            for ((x, y) in tunnelPoints) {
                points.add(Point(x = x, y = y))
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
