fun List<Boolean>.toDecimal(): Int = map { if (it) '1' else '0' }
    .joinToString(separator = "")
    .toInt(2)

fun List<String>.countBits() = flatMap { it.toList().mapIndexed { pos, bit -> Pair(pos, bit) } }
    .filter { (_, bit) -> bit == '1' }
    .groupingBy { (pos, _) -> pos }
    .eachCount()

fun mostCommon(onesCount: Int, leftCount: Int, keepMostCommon: Boolean): Boolean {
    val predominanceCount = leftCount.toFloat() / 2
    return if (keepMostCommon) {
        onesCount >= predominanceCount
    } else {
        onesCount <= predominanceCount
    }
}

fun findRatingNumbers(
    numbers: List<String>, decisive: Char,
    chooseMostValue: Boolean,
): Int {
    val bits = numbers.first().length
    var numbersLeft = numbers.toSet()
    for (pos in 0 until bits) {
        val onesCount = numbersLeft.map { it[pos] }.count { it == '1' }
        val keepOnes = mostCommon(onesCount, numbersLeft.size, chooseMostValue)
        val keepZeros = mostCommon(onesCount, numbersLeft.size, !chooseMostValue)
        val numbersToKeep = mutableSetOf<String>()
        for (number in numbersLeft) {
            if (keepOnes && keepZeros) {
                if (number[pos] == decisive) {
                    numbersToKeep.add(number)
                }
            } else {
                if (number[pos] == '1' && keepOnes) {
                    numbersToKeep.add(number)
                } else if (number[pos] == '0' && keepZeros) {
                    numbersToKeep.add(number)
                }
            }
        }
        numbersLeft = numbersToKeep
        if (numbersLeft.size == 1) {
            break
        }
    }
    if (numbersLeft.size != 1) {
        error("Not exactly one number left!")
    }
    return numbersLeft.first().toInt(2)
}

fun main() {
    fun part1(input: List<String>): Int {
        val counts = input.countBits()
            .toSortedMap()
            .map { (_, count) -> count }
        val gammaRate = counts
            .map { count -> count > input.size / 2 }
            .toDecimal()
        val epsilonRate = counts
            .map { count -> count < input.size / 2 }
            .toDecimal()
        return gammaRate * epsilonRate
    }

    fun part2(input: List<String>): Int {
        val oxygenGeneratorRating = findRatingNumbers(input, '1', true)
        val co2ScrubberRating = findRatingNumbers(input, '0', false)
        return oxygenGeneratorRating * co2ScrubberRating
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
