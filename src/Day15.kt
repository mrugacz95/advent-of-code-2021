import java.util.PriorityQueue

private class RiskMap(val rawMap: List<List<Int>>) {
    val nodes = mutableMapOf<Point, MutableList<WeightedEdge>>().withDefault { mutableListOf() }
    val width = rawMap.size
    val height = rawMap.first().size
    val defaultEnd = Point(height - 1, width - 1)
    private val adjacent = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, 1), Pair(0, -1))

    init {
        for (y in 0 until height) {
            for (x in 0 until width) {
                for ((ny, nx) in getNeighbours(y, x, width, height, adjacent)) {
                    addEdge(Point(ny, nx), Point(y, x), rawMap[y][x])
                    addEdge(Point(y, x), Point(ny, nx), rawMap[ny][nx])
                }
            }
        }
    }

    private fun addEdge(start: Point, end: Point, weight: Int) {
        nodes[start] = nodes.getValue(start).apply { add(WeightedEdge(end, weight)) }
    }

    fun dijkstra(start: Point = Point.ZERO, end: Point = defaultEnd): Int {
        val dist = mutableMapOf<Point, Int>().withDefault { Int.MAX_VALUE }
        val q = PriorityQueue(Comparator<Point> { p1, p2 -> dist.getValue(p1).compareTo(dist.getValue(p2)) })
        q.add(start)
        dist[start] = 0
        while (q.isNotEmpty()) {
            val current = q.poll()
            if (current == end) break
            for ((node, weight) in nodes.getValue(current)) {
                if (dist.getValue(current) + weight < dist.getValue(node)) {
                    dist[node] = dist.getValue(current) + weight
                    q.add(node)
                }
            }
        }
        return dist.getValue(end)
    }

    fun calcRisk(point: Point): Int = (rawMap[point.y % height][point.x % width] + point.y / height + point.x / width - 1) % 9 + 1

    fun repeatableDijkstra(start: Point = Point.ZERO, repetition: Int = 5): Int {
        val dist = mutableMapOf<Point, Int>().withDefault { Int.MAX_VALUE }
        val q = PriorityQueue(Comparator<Point> { p1, p2 -> dist.getValue(p1).compareTo(dist.getValue(p2)) })
        val repeatedWidth = width * repetition
        val repeatedHeight = height * repetition
        val repeatedEnd = Point(repeatedHeight - 1, repeatedWidth - 1)
        q.add(start)
        dist[start] = 0
        while (q.isNotEmpty()) {
            val current = q.poll()
            if (current == repeatedEnd) break
            for (neighbour in getNeighbours(current.y, current.x, repeatedWidth, repeatedHeight, adjacent)) {
                if (dist.getValue(current) + calcRisk(neighbour) < dist.getValue(neighbour)) {
                    dist[neighbour] = dist.getValue(current) + calcRisk(neighbour)
                    q.add(neighbour)
                }
            }
        }
        return dist.getValue(repeatedEnd)
    }
}

fun main() {
    fun parseInput(input: List<String>): RiskMap {
        return RiskMap(input.map { row -> row.toList().map { Character.getNumericValue(it) } })
    }

    fun part1(riskMap: RiskMap): Int {
        return riskMap.dijkstra()
    }

    fun part2(riskMap: RiskMap, repetition: Int = 5): Int {
        return riskMap.repeatableDijkstra(repetition = repetition)
    }

    val dayId = "15"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 40)
    check(part2(testInput, 1) == 40)
    val smallInput = parseInput(readInput("Day${dayId}_small"))
    val repeatedMap = RiskMap(listOf(listOf(8)))
    check((0..4).map { y -> (0..4).map { x -> repeatedMap.calcRisk(Point(y, x)) } } == smallInput.rawMap)
    check(part1(smallInput) == part2(repeatedMap, 5))
    check(part2(testInput) == 315)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
