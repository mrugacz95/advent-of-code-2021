private val diracDice = (1..3).cartesianProduct((1..3)).cartesianProduct(1..3).map { (t1t2, t3) ->
    val (t1, t2) = t1t2
    t1 + t2 + t3
}.groupingBy { it }.eachCount()

fun <T> Pair<T, T>.get(i: Int): T {
    return when (i) {
        0    -> first
        1    -> second
        else -> error("No $i element in Pair")
    }
}

fun <T> Pair<T, T>.set(i: Int, value: T): Pair<T, T> {
    return when (i) {
        0    -> Pair(value, second)
        1    -> Pair(first, value)
        else -> error("No $i element in Pair")
    }
}

fun Pair<Long, Long>.add(other: Pair<Long, Long>): Pair<Long, Long> {
    return Pair(first + other.first, second + other.second)
}

fun main() {
    fun parseInput(input: List<String>): IntArray {
        val result = input.map {
            "Player \\d+ starting position: (?<position>\\d+)".toRegex().matchEntire(it)!!.groups["position"]!!.value.toInt()
        }
        check(result.size == 2)
        return result.toIntArray()
    }

    fun Int.sequenceSum() = this * (this - 1) / 2

    fun part1(pos: IntArray): Int {
        val rolls = IntArray(2)
        val points = IntArray(2)
        var diceState = 1
        var turn = 0
        while (points.all { it < 1000 }) {
            rolls[turn] += 3
            diceState += 3
            val roll = diceState.sequenceSum() - (diceState - 3).sequenceSum()
            pos[turn] = (pos[turn] + roll - 1) % 10 + 1
            points[turn] += pos[turn]
            diceState = ((diceState - 1) % 100) + 1
            turn = (turn + 1) % 2
        }
        return points.minOf { it } * rolls.sum()
    }

    fun part2(
        pos: Pair<Int, Int>,
        points: Pair<Long, Long> = Pair(0L, 0L),
        turn: Int = 0
    ): Pair<Long, Long> {

        if (points.toList().any { it >= 21 }) {
            return if (points.first > points.second) {
                Pair(1, 0)
            } else {
                Pair(0, 1)
            }
        }
        var p1Wins = 0L
        var p2Wins = 0L
        for ((diceResult, univCount) in diracDice) {
            val newPos = pos.set(turn, (pos.get(turn) + diceResult - 1) % 10 + 1)
            val newPoints = points.copy().set(turn, points.get(turn) + newPos.get(turn).toLong())
            val newTurn = (turn + 1) % 2
            val (p1, p2) = part2(newPos, newPoints, newTurn)
            p1Wins += p1 * univCount
            p2Wins += p2 * univCount
        }
        return Pair(p1Wins, p2Wins)
    }

    val dayId = "21"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput.clone()) == 739785)
    check(part2(Pair(testInput[0], testInput[1])).toList().maxOf { it } == 444356092776315L)
    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input.clone()))
    println(part2(Pair(input[0], input[1])).toList().maxOf { it })
}