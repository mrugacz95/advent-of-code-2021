import java.util.Stack

private const val LOG = false

typealias AmpsState = Map<Point, String>

private fun String.cost() = mapOf(
    'A' to 1,
    'B' to 10,
    'C' to 100,
    'D' to 1000
)[this.type()]!!

private fun String.col() = mapOf(
    'A' to 3,
    'B' to 5,
    'C' to 7,
    'D' to 9
)[this.type()]!!

private fun String.type() = this[0]

data class Burrow(val graph: Map<Point, MutableList<WeightedEdge>>, val amphipods: MutableMap<Point, String>)

fun main() {
    fun parseInput(input: List<String>): Burrow {
        val adjacent = listOf(Pair(-1, 0), Pair(0, 1), Pair(1, 0), Pair(0, -1))
        val graph = mutableMapOf<Point, MutableList<WeightedEdge>>().withDefault { mutableListOf() }
        val height = input.size
        val width = input.first().length
        val amphipods = mutableMapOf<Point, String>()
        for ((y, row) in input.withIndex()) {
            for ((x, cell) in row.withIndex()) {
                if (y >= 2 && x <= 2) continue
                if (y >= 2 && x >= 10) continue
                if (cell != '#') {
                    for ((ny, nx) in getNeighbours(y, x, width, height, adjacent)) {
                        if (input[ny][nx] != '#') {
                            graph[Point(y, x)] = graph.getValue(Point(y, x)).apply { add(WeightedEdge(Point(ny, nx), 1)) }
                        }
                    }
                }
                if (cell in 'A'..'D') {
                    val num = if (cell in amphipods.values.map { it[0] }) "1" else "2"
                    amphipods[Point(y, x)] = "$cell$num"
                }
            }
        }
        for ((x, _) in input[1].withIndex()) {
            if (input[1][x] == '.' && input[2][x - 1] == '#' && input[2][x] != '#' && input[2][x + 1] == '#') { // room entrance
                val neighbours = graph.getValue(Point(1, x)).toSet()
                graph.remove(Point(1, x))
                for (neighbour in neighbours) {
                    graph[neighbour.end] = graph.getValue(neighbour.end).apply { remove(WeightedEdge(Point(1, x), 1)) }
                    val newNeighbours = neighbours.minus(neighbour).map { WeightedEdge(it.end, 2) }
                    graph[neighbour.end] = graph.getValue(neighbour.end).apply { addAll(newNeighbours) } // without self
                }
            }
        }
        return Burrow(graph, amphipods)
    }

    fun areOrganized(amps: AmpsState): Boolean {
        for ((pos, a) in amps) {
            if (pos.y == 1 || a.col() != pos.x) {
                return false
            }
        }
        return true
    }

    fun printState(amps: AmpsState, graph: Map<Point, MutableList<WeightedEdge>>): String {
        val height = graph.keys.maxOf { it.y } - 1
        val inner = mutableListOf<MutableList<Char>>()
        for (repeat in 0 until height - 2) {
            inner.add("  #.#.#.#.#".toMutableList())
        }
        val board = listOf(
            "#############".toMutableList(),
            "#...........#".toMutableList(),
            "###.#.#.#.###".toMutableList()
        ) + inner + listOf(
            "  #########".toMutableList()
        )

        for ((pos, a) in amps) {
            val toPrint = if (a[1] == '1') a.lowercase()[0] else a[0]
            board[pos.y][pos.x] = toPrint
        }
        return board.joinToString("\n") { it.joinToString("") } + "\n"
    }

    fun simulate(burrow: Burrow): Pair<Int, List<AmpsState>> {
        val stateVisited = mutableMapOf<AmpsState, Int>()
        var globMinCost = Int.MAX_VALUE
        var globHistory = emptyList<AmpsState>()

        fun step(graph: Map<Point, MutableList<WeightedEdge>>, amps: AmpsState, currentCost: Int, history: List<AmpsState>): Int {
            val height = graph.keys.maxOf { it.y }
            if (LOG) {
                println(printState(amps, graph))
            }
            if (areOrganized(amps)) {
                globHistory = history
                globMinCost = minOf(globMinCost, currentCost)
                return 0
            }
            if (currentCost >= globMinCost) {
                return Int.MAX_VALUE
            }
            if (amps in stateVisited.keys) return stateVisited[amps]!!
            var minCost = Int.MAX_VALUE
            stateVisited[amps.toMap()] = minCost
            for ((pos, amp) in amps.entries) {
                val dist = mutableMapOf<Point, Int>()
                dist[pos] = 0
                // calc possible moves with dfs
                val stack = Stack<Point>()
                stack.add(pos)
                val allMoves = mutableSetOf<Point>()
                while (stack.isNotEmpty()) {
                    val current = stack.pop()
                    allMoves.add(current)
                    for ((n, weight) in graph.getValue(current)) {
                        if (n in amps.keys) continue // occupied
                        if (n !in allMoves) { // already visited
                            stack.add(n)
                            dist[n] = minOf(dist[n] ?: Int.MAX_VALUE, dist[current]!! + weight)
                        }
                    }
                }
                // remove stupid moves
                var goodMoves = mutableSetOf<Point>()
                for (target in allMoves) {
                    if (pos == target) continue // don't stay in place
                    if (pos.y == 1 && target.y == 1) continue // don't wander in hallway
                    if (target.y >= 2 && target.x != amp.col()) continue // don't go into someone's house
                    if (target.y >= 2 &&
                        ((target.y + 1)..height).map { Point(it, target.x) }.any { it !in amps }
                    ) continue // don't stop in first room, go further if possible
                    if (target.y >= 2 &&
                        ((target.y + 1)..height).map { Point(it, target.x) }.any { amps[it]!!.type() != amp.type() }
                    ) continue // don't go in your house when stranger inside
                    if (target.x == pos.x && pos.y >= 2) continue // don't wander in house
                    if (pos.y >= 2 &&
                        pos.x == amp.col() &&
                        ((pos.y + 1)..height).map { Point(it, pos.x) }.all { amps[it]!!.type() == amp.type() }
                    ) { // don't go outside if on good position
                        continue
                    }
                    goodMoves.add(target)
                }
                if (goodMoves.any { it.y != 1 }) {
                    goodMoves = goodMoves.filter { it.y != 1 }.toMutableSet()
                }

                for (target in goodMoves) {
                    val moveCost = amp.cost() * dist[target]!!
                    if (LOG) {
                        println("$amp from $pos to $target cost: $moveCost")
                    }
                    val newState = amps.minus(pos).toMutableMap().apply { put(target, amp) }
                    val stepCost = step(
                        graph, newState,
                        currentCost + moveCost,
                        history + listOf(newState)
                    )
                    if (stepCost != Int.MAX_VALUE) {
                        minCost = minOf(minCost, moveCost + stepCost)
                    }
                }
            }
            stateVisited[amps.toMap()] = minCost
            return minCost
        }

        val result = step(burrow.graph, burrow.amphipods, 0, emptyList())
        return Pair(result, globHistory)
    }

    fun printHistory(history: List<AmpsState>, graph: Map<Point, MutableList<WeightedEdge>>) {
        println("Best history:")
        for (h in history) {
            println(printState(h, graph))
        }
    }

    fun part1(input: List<String>, showResult: Boolean = false): Int {
        val burrow = parseInput(input)
        val (result, history) = simulate(burrow)
        if (showResult) printHistory(history, burrow.graph)
        return result
    }

    fun part2(input: List<String>, showResult: Boolean = false): Int {
        val burrow = parseInput(input.slice(0..2) + "  #D#C#B#A#" + "  #D#B#A#C#" + input.slice(3..4))
        val (result, history) = simulate(burrow)
        if (showResult) printHistory(history, burrow.graph)
        return result
    }

    val dayId = "23"

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day${dayId}_test")
    check(part1(testInput, false) == 12521)
    check(part2(testInput, false) == 44169)

    val input = readInput("Day${dayId}")
    println(part1(input, showResult = true))
    println(part2(input, showResult = true))
}