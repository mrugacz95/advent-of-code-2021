data class Entry(val patterns: List<Set<Char>>, val output: List<Set<Char>>) {
    fun decode(): Int {
        val one = patterns.single { it.size == 2 }
        val four = patterns.single { it.size == 4 }
        val seven = patterns.single { it.size == 3 }
        val eight = patterns.single { it.size == 7 }
        val nine = patterns.single { it.size == 6 && (it.toSet() intersect four).size == 4 }
        val six = patterns.single { it.size == 6 && (it.toSet() intersect seven).size == 2 }
        val two = patterns.single { it.size == 5 && (it intersect nine).size == 4 }
        val five = patterns.single { it.size == 5 && (it intersect six).size == 5 }
        val three = patterns.single { it.size == 5 && (it intersect seven).size == 3 }
        val zero = patterns.single { it.size == 6 && !it.containsAll(nine intersect six)}
        val decryption = listOf(zero, one, two ,three, four, five, six, seven, eight, nine)
        return output.joinToString("") { decryption.indexOf(it).toString() }.toInt()
    }
}

fun main() {
    fun parseInput(input: List<String>): List<Entry> {
        return input.map { entry ->
            val (pattern, output) = entry.split(" | ").map { it.split(' ') }
            Entry(pattern.map { it.toSet() }, output.map { it.toSet() })
        }
    }

    fun part1(input: List<Entry>): Int {
        val uniqueSegments = setOf(2, 4, 3, 7)
        return input.map { it.output }.flatten().count { it.size in uniqueSegments }
    }

    fun part2(input: List<Entry>): Int {
        return input.sumOf { it.decode() }
    }

    val dayId = "08"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
