fun main() {
    fun parseInput(input: List<String>): List<Int> {
        return input.first().split(',').map { it.toInt() }
    }

    fun part1(input: List<Int>): Int {
        var fishList = input
        for (generation in 0 until 80) {
            val newFish = mutableListOf<Int>()
            val oldFish = mutableListOf<Int>()
            for (fish in fishList) {
                val nextValue = if (fish == 0) 6 else fish - 1
                oldFish.add(nextValue)
                if (fish == 0) {
                    newFish.add(8)
                }
            }
            fishList = oldFish + newFish
        }
        return fishList.size
    }

    fun part2(input: List<Int>): Long {
        val generations = 256
        fun step(before: Map<Int, Long>): Map<Int, Long> {
            val after = mutableMapOf<Int, Long>()
            for ((timer, count) in before){
                val nextValue = if (timer == 0) 6 else timer - 1
                after[nextValue] = (after[nextValue] ?: 0L) + count
                if (timer == 0) {
                    after[8] = count
                }
            }
            return after
        }
        var fishMap = input.groupingBy { it }.eachCount().mapValues { it.value.toLong() }
        for (generation in 0 until generations){
            fishMap = step(fishMap)
        }
        return fishMap.values.sum()
    }

    val dayId = "06"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput) == 5934)
    check(part2(testInput) == 26984457539L)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}
