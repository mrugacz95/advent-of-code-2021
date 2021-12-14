fun main() {

    fun parseInput(input: List<String>): Pair<String, Map<String, Char>> {
        return Pair(input.first(), input.drop(2).associate {
            val (pair, inserted) = it.split(" -> ")
            pair to inserted.single()
        })
    }

    fun part1(input: Pair<String, Map<String, Char>>): Int {
        val (initial, rules) = input
        var polymer = initial
        for (step in 1..10) {
            var newPolymer = polymer.first().toString()
            for (position in 0..polymer.length - 2) {
                val pair = polymer.subSequence(position, position + 2)
                newPolymer += rules.getOrDefault(pair, "").toString() + pair.last()
            }
            polymer = newPolymer
        }
        val sorted = polymer.groupingBy { it }.eachCount().values.sorted()
        return sorted.last() - sorted.first()
    }

    fun part2(input: Pair<String, Map<String, Char>>, steps: Int = 40): Long {

        val (initial, rules) = input
        val cache = mutableMapOf<Pair<Int, String>, Map<Char, Long>>()

        fun applyRules(step: Int, fragment: String): Map<Char, Long> {
            cache[Pair(step, fragment)]?.let { return it }
            val counts = mutableMapOf<Char, Long>().withDefault { 0L }
            if (step == steps) return emptyMap()
            if (step == 0) fragment.map { counts[it] = counts.getValue(it) + 1L }
            for (pair in fragment.windowed(2)) {
                val insertion = rules.getOrDefault(pair, null)
                if (insertion != null) {
                    counts[insertion] = counts.getValue(insertion) + 1L
                    val deeperCounts = applyRules(step + 1, pair.first() + insertion.toString() + pair.last())
                    deeperCounts.entries.map { (c, count) ->
                        counts[c] = counts.getValue(c) + count
                    }
                }
            }
            cache[Pair(step, fragment)] = counts
            return counts
        }

        val counts = applyRules(0, initial)
        val sorted = counts.values.sorted()
        return sorted.last() - sorted.first()
    }

    val dayId = "14"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 1588)
    check(part2(testInput, 10) == 1588L)
    check(part2(testInput) == 2188189693529L)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
