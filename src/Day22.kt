data class Cuboid(val x: IntRange, val y: IntRange, val z: IntRange)

fun main() {
    fun parseInput(input: List<String>): List<Pair<Boolean, Cuboid>> {
        return input.map {
            val groups =
                "(?<state>on|off) x=(?<xStart>-?\\d+)..(?<xEnd>-?\\d+),y=(?<yStart>-?\\d+)..(?<yEnd>-?\\d+),z=(?<zStart>-?\\d+)..(?<zEnd>-?\\d+)".toRegex()
                    .matchEntire(it)!!.groups
            val state = groups["state"]!!.value == "on"
            Pair(
                state,
                Cuboid(
                    groups["xStart"]!!.value.toInt()..groups["xEnd"]!!.value.toInt(),
                    groups["yStart"]!!.value.toInt()..groups["yEnd"]!!.value.toInt(),
                    groups["zStart"]!!.value.toInt()..groups["zEnd"]!!.value.toInt()
                )
            )
        }
    }

    fun mark(reactor: MutableMap<Triple<Int, Int, Int>, Boolean>, value: Boolean, cuboid: Cuboid) {
        for (z in cuboid.z)
            for (y in cuboid.y)
                for (x in cuboid.x)
                    reactor[Triple(z, y, x)] = value
    }

    fun part1(steps: List<Pair<Boolean, Cuboid>>): Int {
        val size = 50
        val reactor = HashMap<Triple<Int, Int, Int>, Boolean>()
        for ((state, step) in steps) {
            if (step.x.first >= -size && step.x.last <= size &&
                step.y.first >= -size && step.y.last <= size &&
                step.z.first >= -size && step.z.last <= size
            )
                mark(reactor, state, step)
        }
        return reactor.values.count { it }
    }

    val dayId = "22"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 590784)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
}