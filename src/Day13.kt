import java.util.LinkedList
import kotlin.math.abs

private data class Fold(val lineNum: Int, val horizontal: Boolean)

private data class TransparentPaper(private val points: List<Point>, private val initialFolds: List<Fold>) {
    private val folds: MutableList<Fold>
    private var paper: Array<BooleanArray>

    init {
        paper = Array(points.maxOf { it.y } + 1) { BooleanArray(points.maxOf { it.x } + 1) }
        points.map { paper[it.y][it.x] = true }
        folds = initialFolds.toMutableList()
    }

    private val height
        get() = paper.size
    private val width
        get() = paper.first().size

    fun keepFolding() = folds.isNotEmpty()

    fun foldPaper() {
        fun getDimensionsAfterFolding(fold: Fold): Pair<Int, Int> {
            return if (fold.horizontal) {
                Pair(maxOf(height - fold.lineNum - 1, fold.lineNum), width)
            } else {
                Pair(height, maxOf(width - fold.lineNum - 1, fold.lineNum))
            }
        }

        fun getPointToBeFilled(y: Int, x: Int, fold: Fold): Point? {
            return if (fold.horizontal) {
                if (y == fold.lineNum) return null // omit fold line
                Point(fold.lineNum - abs(y - fold.lineNum), x)
            } else {
                if (x == fold.lineNum) return null // omit fold line
                Point(y, fold.lineNum - abs(x - fold.lineNum))
            }
        }

        val fold = folds.removeFirst()
        val (heightAfterFold, widthAfterFold) = getDimensionsAfterFolding(fold)
        val folded = Array(heightAfterFold) { BooleanArray(widthAfterFold) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val filled = getPointToBeFilled(y, x, fold) ?: continue
                folded[filled.y][filled.x] = folded[filled.y][filled.x] || paper[y][x]
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
                    val (direction, lineNum) = row
                        .replace("fold along ", "")
                        .split("=")
                    folds.add(
                        Fold(
                            lineNum = lineNum.toInt(),
                            horizontal = when (direction) {
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
        part2(testInput.copy()) == """#####
                                     |#...#
                                     |#...#
                                     |#...#
                                     |#####
                                     |.....
                                     |.....""".trimMargin()
    )

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input.copy()))
    println(part2(input.copy()))
}
