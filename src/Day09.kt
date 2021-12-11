import java.util.LinkedList

fun main() {
    fun parseInput(input: List<String>): List<List<Int>> {
        return input.map { row -> row.toList().map { Character.getNumericValue(it) } }
    }

    val adjacent = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

    fun findLowerPoints(heightmap: List<List<Int>>): List<Point> {
        val lowerPoints = mutableListOf<Point>()
        val width = heightmap.first().size
        val height = heightmap.size
        for (y in 0 until height) {
            for (x in 0 until width) {
                var isLowPoint = true
                for ((ny, nx) in getNeighbours(y, x, width, height, adjacent)) {
                    if (heightmap[ny][nx] <= heightmap[y][x]) {
                        isLowPoint = false
                    }
                }
                if (isLowPoint) {
                    lowerPoints.add(Point(y, x))
                }
            }
        }
        return lowerPoints
    }

    fun floodFill(startingPoints: List<Point>, heightmap: List<List<Int>>): List<Set<Point>> {
        val width = heightmap.first().size
        val height = heightmap.size
        val basins = mutableListOf<Set<Point>>()
        for (start in startingPoints) {
            val visited = mutableSetOf<Point>()
            val queue = LinkedList<Point>()
            queue.add(start)
            while (queue.isNotEmpty()) {
                val current = queue.pop()
                if (current in visited) {
                    continue
                }
                visited.add(current)
                for ((ny, nx) in getNeighbours(current.first, current.second, width, height, adjacent)) {
                    if (heightmap[ny][nx] != 9) {
                        queue.add(Point(ny, nx))
                    }
                }
            }
            basins.add(visited)
        }
        return basins
    }

    fun part1(input: List<List<Int>>): Int {
        return findLowerPoints(input).sumOf { input[it.first][it.second] + 1 }
    }

    fun part2(input: List<List<Int>>): Int {
        val lowerPoints = findLowerPoints(input)
        val basins = floodFill(lowerPoints, input)
        return basins.map { it.size }.sortedDescending().take(3).reduce { acc, next -> acc * next }
    }

    val dayId = "09"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
