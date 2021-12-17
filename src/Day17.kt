import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

private data class Area(val x: IntRange, val y: IntRange) {
    operator fun contains(p: Point): Boolean {
        return p.x in x && p.y in y
    }
}

private data class SimulationData(val maxYPos: Int)

fun main() {
    fun parseInput(input: List<String>): Area {
        val area = input.single()
        val regex = "target area: x=(?<xStart>-?\\d+)..(?<xEnd>-?\\d+), y=(?<yStart>-?\\d+)..(?<yEnd>-?\\d+)".toRegex()
        val groups = regex.matchEntire(area)!!
        return Area(
            groups.groups["xStart"]!!.value.toInt()..groups.groups["xEnd"]!!.value.toInt(),
            groups.groups["yStart"]!!.value.toInt()..groups.groups["yEnd"]!!.value.toInt(),
        )
    }

    fun simulateProbe(velocity: Point, area: Area): SimulationData? {
        var v = velocity
        var pos = Point(0, 0)
        var maxYPos = 0
        while (pos.y >= area.y.first) {
            pos += v
            maxYPos = maxOf(pos.y, maxYPos)
            if (v.x > 0) // drag
                v = Point(y = v.y, x = v.x - 1)
            v = Point(y = v.y - 1, x = v.x) // gravity
            if (pos in area) {
                return SimulationData(maxYPos)
            }
        }
        return null
    }

    fun part1(area: Area): Int? {
        var maxYPos: Int? = null
        val minXVelocity = floor(sqrt(2.0 * area.x.first)).toInt() - 1
        val maxXVelocity = ceil(sqrt(2.0 * area.x.first)).toInt() + 1
        for (x in minXVelocity..maxXVelocity) {
            for (y in 0..2000) {
                val result = simulateProbe(Point(y, x), area)
                if (result != null) {
                    maxYPos = maxOf(result.maxYPos, maxYPos ?: Int.MIN_VALUE)
                }
            }
        }
        return maxYPos
    }

    fun part2(area: Area): MutableSet<Point> {
        val distinctVelocities = mutableSetOf<Point>()
        for (x in 0..area.x.last) {
            for (y in area.y.first..2000) {
                val result = simulateProbe(Point(y, x), area)
                if (result != null) {
                    distinctVelocities.add(Point(y, x))
                }
            }
        }
        return distinctVelocities
    }

    val dayId = "17"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(testInput == Area(20..30, -10..-5))
    check(part1(testInput) == 45)
    val expectedVelocities = """23,-10  25,-9   27,-5   29,-6   22,-6   21,-7   9,0     27,-7   24,-5
                               |25,-7   26,-6   25,-5   6,8     11,-2   20,-5   29,-10  6,3     28,-7
                               |8,0     30,-6   29,-8   20,-10  6,7     6,4     6,1     14,-4   21,-6
                               |26,-10  7,-1    7,7     8,-1    21,-9   6,2     20,-7   30,-10  14,-3
                               |20,-8   13,-2   7,3     28,-8   29,-9   15,-3   22,-5   26,-8   25,-8
                               |25,-6   15,-4   9,-2    15,-2   12,-2   28,-9   12,-3   24,-6   23,-7
                               |25,-10  7,8     11,-3   26,-7   7,1     23,-9   6,0     22,-10  27,-6
                               |8,1     22,-8   13,-4   7,6     28,-6   11,-4   12,-4   26,-9   7,4
                               |24,-10  23,-8   30,-8   7,0     9,-1    10,-1   26,-5   22,-9   6,5
                               |7,5     23,-6   28,-10  10,-2   11,-1   20,-9   14,-2   29,-7   13,-3
                               |23,-5   24,-8   27,-9   30,-7   28,-5   21,-10  7,9     6,6     21,-5
                               |27,-10  7,2     30,-9   21,-8   22,-7   24,-9   20,-6   6,9     29,-5
                               |8,-2    27,-8   30,-5   24,-7""".trimMargin()
        .split(" +|\n".toRegex())
        .map { it.split(",") }
        .map { (x, y) -> Point(y.toInt(), x.toInt()) }
        .toSet()
    check(part2(testInput) == expectedVelocities)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input).size)
}