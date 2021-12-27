import java.util.Stack

sealed class Operation {
    abstract fun reduce(state: State, input: MutableList<Int>, output: StringBuilder): State
    fun resolveVariable(v: String, state: State): Int {
        if (v[0] in 'w'..'z') return state[v]!!
        return v.toInt()
    }
}

data class Add(val a: String, val b: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, state[a]!! + resolveVariable(b, state)) }
}

data class Mul(val a: String, val b: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, state[a]!! * resolveVariable(b, state)) }
}

data class Inp(val a: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, input.removeFirst()) }
}

data class Div(val a: String, val b: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, state[a]!! / resolveVariable(b, state)) }
}

data class Mod(val a: String, val b: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, state[a]!! % resolveVariable(b, state)) }
}

data class Eql(val a: String, val b: String) : Operation() {
    override fun reduce(state: State, input: MutableList<Int>, output: StringBuilder) =
        state.apply { replace(a, (state[a]!! == resolveVariable(b, state)).toInt()) }
}

typealias State = MutableMap<String, Int>

fun main() {
    fun getInitState(): MutableMap<String, Int> = mutableMapOf(
        "w" to 0,
        "x" to 0,
        "y" to 0,
        "z" to 0,
    )

    fun runOperations(operations: List<Operation>, initInput: List<Int>): Map<String, Int> {
        val input = initInput.toMutableList()
        var state = getInitState()
        val output = StringBuilder()
        for (op in operations) {
            state = op.reduce(state, input, output)
        }
        return state
    }

    fun parseIntoOperations(input: List<String>): List<Operation> {
        return input.map { line ->
            val parts = line.split(" ")
            when {
                parts[0] == "inp" -> {
                    Inp(parts[1])
                }
                parts[0] == "add" -> {
                    Add(parts[1], parts[2])
                }
                parts[0] == "mul" -> {
                    Mul(parts[1], parts[2])
                }
                parts[0] == "div" -> {
                    Div(parts[1], parts[2])
                }
                parts[0] == "mod" -> {
                    Mod(parts[1], parts[2])
                }
                parts[0] == "eql" -> {
                    Eql(parts[1], parts[2])
                }
                else              -> error("Invalid operation: $line")
            }
        }
    }

    fun splitDifference(sum: Int): Set<Pair<Int, Int>> {
        val pairs = mutableSetOf<Pair<Int, Int>>()
        for (digit1 in 1..9) {
            for (digit2 in 1..9) {
                if (digit1 - digit2 == sum) {
                    pairs.add(Pair(digit1, digit2))
                }
            }
        }
        return pairs
    }

    fun resolve(operations: List<Operation>, pairSelector: (List<Pair<Int, Int>>) -> Pair<Int, Int>): String {
        val inputNumbers = 14
        val chunkSize = operations.size / inputNumbers
        val constrains = Array(inputNumbers) { IntArray(3) }
        for ((cIdx, chunk) in operations.chunked(chunkSize).withIndex()) {
            constrains[cIdx][0] = (chunk[4] as Div).b.toInt()
            constrains[cIdx][1] = (chunk[5] as Add).b.toInt()
            constrains[cIdx][2] = (chunk[15] as Add).b.toInt()
        }
        val matches = mutableListOf<Pair<Int, Int>>()
        val stack = Stack<Int>()
        for ((idx, chunk) in constrains.withIndex()) {
            if (chunk[0] == 1) {
                stack.add(idx)
            } else {
                val match = stack.pop()
                matches.add(Pair(idx, match))
            }
        }
        val serialNumber = IntArray(14)
        for ((r1, r2) in matches) {
            val diff = constrains[r1][1] + constrains[r2][2]
            val (digit1, digit2) = splitDifference(diff).sortedBy { it.first * 10 + it.second }.let(pairSelector)
            serialNumber[r1] = digit1
            serialNumber[r2] = digit2
        }
        // check
        check(runOperations(operations, serialNumber.toList())["z"]!! == 0)
        return serialNumber.joinToString("") { it.toString() }
    }

    fun part1(operations: List<Operation>): String {
        return resolve(operations) { it.first() }
    }

    fun part2(operations: List<Operation>): String {
        return resolve(operations) { it.last() }
    }

    val dayId = "24"

    // test if implementation meets criteria from the description, like:
    check(runOperations(listOf(Inp("x"), Mul("x", "-1")), listOf(14))["x"] == -14)
    check(runOperations(listOf(Inp("z"), Inp("x"), Mul("z", "3"), Eql("z", "x")), listOf(14, 14 * 3))["z"] == 1)
    val testInput = parseIntoOperations(readInput("Day${dayId}_test"))
    val a = runOperations(testInput, listOf(10))
    check(a == mapOf("w" to 1, "x" to 0, "y" to 1, "z" to 0))

    val operations = parseIntoOperations(readInput("Day${dayId}"))
    println(part1(operations))
    println(part2(operations))
}