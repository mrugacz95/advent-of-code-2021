enum class Direction { FORWARD, DOWN, UP }
data class Command(val direction: Direction, val units: Int)

fun parseInput(input: List<String>) = input
    .map { it.split(" ") }
    .map { (cmd, unit) ->
        Command(Direction.valueOf(cmd.uppercase()), unit.toInt())
    }

fun main() {
    fun part1(input: List<Command>): Int {
        var horizontal = 0
        var depth = 0
        for(command in input){
            with(command) {
                when (direction) {
                    Direction.FORWARD -> horizontal += units
                    Direction.DOWN    -> depth += units
                    Direction.UP      -> depth -= units
                }
            }
        }
        return horizontal * depth
    }

    fun part2(input: List<Command>): Int {
        var horizontal = 0
        var depth = 0
        var aim = 0
        for(command in input){
            with(command) {
                when (direction) {
                    Direction.FORWARD -> {
                        horizontal += units
                        depth += aim * units
                    }
                    Direction.DOWN    -> {
                        aim += units
                    }
                    Direction.UP      -> {
                        aim -= units
                    }
                }
            }
        }
        return horizontal * depth
    }

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day02_test"))
    check(part1(testInput) == 150)
    check(part2(testInput) == 900)

    val input = parseInput(readInput("Day02"))
    println(part1(input))
    println(part2(input))
}
