data class InfiniteImage(val content: Set<Point>, val area: Area, val outerValue: Int = 0) {
    fun getBit(position: Point): Int = if (position in area) {
        if (position in content) {
            1
        } else {
            0
        }
    } else {
        outerValue
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (y in this.area.y) {
            for (x in this.area.x) {
                builder.append(
                    if (Point(y, x) in this.content) {
                        '#'
                    } else {
                        '.'
                    }
                )
            }
            builder.append('\n')
        }
        return builder.toString()
    }
}

fun Boolean.toInt() = if (this) 1 else 0

class ScannerResponse(val iea: BooleanArray, val image: InfiniteImage)

fun main() {
    fun parseInput(input: List<String>): ScannerResponse {
        val imageEnhancementAlgorithm = input.first().map { it == '#' }.toBooleanArray()
        val inputImage = input.subList(2, input.size).flatMapIndexed { y, row ->
            row.mapIndexed { x, v ->
                if (v == '#') {
                    Point(y, x)
                } else {
                    null
                }
            }.filterNotNull()
        }.toSet()
        return ScannerResponse(imageEnhancementAlgorithm, InfiniteImage(inputImage, Area.fromPoints(inputImage)))
    }

    fun step(iea: BooleanArray, image: InfiniteImage): InfiniteImage {
        val imageRange = image.area
        val output = mutableSetOf<Point>()
        for (y in imageRange.y.first - 3..imageRange.y.last + 3) {
            for (x in imageRange.x.first - 3..imageRange.x.last + 3) {
                var number = 0
                for ((idx, delta) in (-1..1).cartesianProduct(-1..1).reversed().withIndex()) {
                    val (yd, xd) = delta
                    val pos = Point(y + yd, x + xd)
                    val bit = image.getBit(pos)
                    number = number or (bit shl idx)
                }
                val resultBit = iea[number]
                if (resultBit) {
                    output.add(Point(y, x))
                }
            }
        }
        val outer = iea[image.outerValue.toString().repeat(9).toInt(2)].toInt()
        return InfiniteImage(content = output, area = Area.fromPoints(output), outerValue = outer)
    }

    fun simulate(input: ScannerResponse, steps: Int): Int {
        var image = input.image
        for (step in 0 until steps) {
            image = step(input.iea, image)
        }
        return image.content.size
    }

    val dayId = "20"

    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(simulate(testInput, 2) == 35)

    val input = parseInput(readInput("Day${dayId}"))
    println(simulate(input, 2))
    println(simulate(input, 50))
}