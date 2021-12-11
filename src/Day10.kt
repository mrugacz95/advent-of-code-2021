import java.util.Stack

sealed class BracketError {
    data class Incomplete(val score: Long) : BracketError()
    data class Invalid(val score: Int) : BracketError()
}

fun main() {
    fun parseInput(input: List<String>): List<List<Char>> {
        return input.map { it.toList() }
    }

    val matchingBrackets = mapOf(
        '{' to '}',
        '(' to ')',
        '[' to ']',
        '<' to '>'
    )

    val invalidBracketScore = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137,
    )

    val incompleteBracketScore = mapOf(
        ')' to 1,
        ']' to 2,
        '}' to 3,
        '>' to 4,
    )

    fun countScores(input: List<List<Char>>): List<BracketError> {

        return input.map { line ->
            val stack = Stack<Char>()
            var invalidScore = 0
            for (bracket in line) {
                if (bracket in matchingBrackets.keys) {
                    stack.add(bracket)
                } else {
                    if (matchingBrackets[stack.peek()] != bracket && invalidScore == 0) {
                        invalidScore = invalidBracketScore[bracket] ?: error("No matching score for $bracket")
                        // Expected ${matchingBrackets[stack.peek()]}, but found $bracket instead.
                    }
                    stack.pop()
                }
            }
            if (invalidScore != 0) {
                BracketError.Invalid(invalidScore)
            } else {
                var incompleteScore = 0L
                while (stack.isNotEmpty()) {
                    incompleteScore *= 5
                    val bracket = stack.pop()
                    incompleteScore += incompleteBracketScore[matchingBrackets[bracket]]
                        ?: error("No matching score for $bracket")
                }
                BracketError.Incomplete(incompleteScore)
            }
        }
    }

    fun part1(input: List<List<Char>>): Int {
        return countScores(input).filterIsInstance<BracketError.Invalid>().sumOf { it.score }
    }

    fun part2(input: List<List<Char>>): Long {
        val scores = countScores(input).filterIsInstance<BracketError.Incomplete>().map { it.score }.sorted()
        return scores[scores.size / 2]
    }

    val dayId = "10"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 26397)
    check(part2(testInput) == 288957L)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
