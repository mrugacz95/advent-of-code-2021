import java.util.LinkedList
import kotlin.math.abs

private data class Fold(val lineNum: Int, val horizontal: Boolean)

private class TransparentPaper(private val points: List<Point>, private val initialFolds: List<Fold>) {
    private val folds = initialFolds.toMutableList()
    private var paper: Array<BooleanArray>

    private val height
        get() = paper.size
    private val width
        get() = paper.first().size

    init {
        paper = Array(points.maxOf { it.first } + 1) { BooleanArray(points.maxOf { it.second } + 1) }
        points.map { paper[it.first][it.second] = true }
    }

    fun keepFolding() = folds.isNotEmpty()

    fun foldPaper() {
        val fold = folds.removeFirst()
        if (fold.horizontal) {
            foldHorizontally(fold.lineNum)
        } else {
            foldVertically(fold.lineNum)
        }
    }

    private fun foldHorizontally(yFold: Int) {
        val heightAfterFold = maxOf(height - yFold - 1, yFold)
        val folded = Array(heightAfterFold) { BooleanArray(width) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                val yFilled = yFold - abs(y - yFold)
                if (y == yFold) continue // designate middle line
                folded[yFilled][x] = folded[yFilled][x] || paper[y][x]
            }
        }
        paper = folded
    }

    private fun foldVertically(xFold: Int) {
        val widthAfterFold = maxOf(width - xFold - 1, xFold)
        val folded = Array(height) { BooleanArray(widthAfterFold) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val xFilled = xFold - abs(x - xFold)
                if (x == xFold) continue // designate middle line
                folded[y][xFilled] = folded[y][xFilled] || paper[y][x]
            }
        }
        paper = folded
    }

    override fun toString(): String {
        return paper.joinToString("\n") { row ->
            row.joinToString("") { if (it) "#" else "." }
        }
    }

    fun count(): Int {
        return paper.sumOf { row -> row.count { it } }
    }

    fun copy() = TransparentPaper(points, initialFolds)
}

fun main() {

    fun parseInput(input: List<String>): TransparentPaper {
        val points = mutableListOf<Point>()
        val folds = LinkedList<Fold>()
        for (row in input) {
            when {
                row.matches("\\d+,\\d+".toRegex()) -> {
                    val (x, y) = row.split(',')
                    points.add(Point(y.toInt(), x.toInt()))
                }
                row.isEmpty()                      -> continue
                row.take(4) == "fold"              -> {
                    val (direction, lineNum) = row.replace("fold along ", "").split("=")
                    folds.add(
                        Fold(
                            lineNum.toInt(),
                            when (direction) {
                                "x"  -> false
                                "y"  -> true
                                else -> error("Invalid direction: $direction")
                            }
                        )
                    )
                }
                else                               -> error("Invalid row: \"$row\"")

            }
        }
        return TransparentPaper(points, folds)
    }

    fun part1(input: TransparentPaper): Int {
        input.foldPaper()
        return input.count()
    }

    fun part2(input: TransparentPaper): String {
        while (input.keepFolding()) {
            input.foldPaper()
        }
        return input.toString()
    }

    val dayId = "13"
    // test if implementation meets criteria from the description, like:
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput.copy()) == 17)
    check(
        part2(testInput.copy()) == """
            #####
            #...#
            #...#
            #...#
            #####
            .....
            .....""".drop(1).trimIndent()
    )

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input.copy()))
    println(part2(input.copy()))
}
