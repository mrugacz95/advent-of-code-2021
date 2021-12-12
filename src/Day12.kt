typealias Edge = Pair<String, String>

fun String.isLowerCase() = this.all { it.isLowerCase() }

class Graph(private val logging: Boolean = false) {

    private val adjacencyList = mutableMapOf<String, MutableList<String>>()

    fun addEdge(edge: Edge) {
        adjacencyList.computeIfAbsent(edge.first) { mutableListOf() }.add(edge.second)
        adjacencyList.computeIfAbsent(edge.second) { mutableListOf() }.add(edge.first)
    }

    fun dfs(
        current: String,
        visited: MutableMap<String, Int>,
        visitedInOrder: List<String> = emptyList(),
        enterCondition: (current: String, visited: Map<String, Int>) -> Boolean
    ): Int {
        val edges = adjacencyList[current] ?: error("Node \"$current\" not found in graph")
        if (current == "end") {
            if (logging) {
                println((visitedInOrder + listOf(current)).joinToString(","))
            }
            return 1
        }
        if (current.isLowerCase() && !enterCondition(current, visited)) {
            return 0
        }
        visited[current] = visited.getOrDefault(current, 0) + 1
        var newPaths = 0
        for (edge in edges) {
            newPaths += dfs(edge, visited, visitedInOrder + listOf(current), enterCondition)
        }
        visited[current] = (visited[current] ?: error("Node \"$current\" expected to be already visited")) - 1
        return newPaths
    }
}

fun main() {
    fun parseInput(input: List<String>): Graph {
        val edges = input.map { it.split("-") }.map { (u, v) -> Edge(u, v) }
        val graph = Graph()
        for (edge in edges) {
            graph.addEdge(edge)
        }
        return graph
    }

    fun part1(input: Graph): Int = input.dfs("start", mutableMapOf()) { curr, vis ->
        vis.getOrDefault(curr, 0) == 0
    }

    fun part2(input: Graph): Int = input.dfs("start", mutableMapOf()) { curr, vis ->
        val visitedTwice = vis.filter { it.key.isLowerCase() && it.value == 2 }.keys.singleOrNull()
        if (visitedTwice == null && curr != "start") {
            true // can visit any node second time
        } else {
            vis.getOrDefault(curr, 0) == 0
        }
    }

    val dayId = "12"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 10)
    check(part2(testInput) == 36)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
