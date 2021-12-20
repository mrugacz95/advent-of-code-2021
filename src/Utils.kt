import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

/**
 * Return cartesian product of two collections
 */
infix fun <T, U> Iterable<T>.cartesianProduct(other: Iterable<U>): List<Pair<T, U>> {
    return this.flatMap { lhsElem -> other.map { rhsElem -> lhsElem to rhsElem } }
}

infix fun Int.toward(to: Int): IntProgression {
    val step = if (this > to) -1 else 1
    return IntProgression.fromClosedRange(this, to, step)
}

fun Pair<Int, Int>.toPoint() = Point(first, second)
data class Point(val y: Int, val x: Int) {
    companion object {
        val ZERO = Point(0, 0)
    }

    operator fun plus(other: Point): Point {
        return Point(y + other.y, x + other.x)
    }
}

fun getNeighbours(y: Int, x: Int, width: Int, height: Int, adjacent: List<Pair<Int, Int>>): MutableList<Point> {
    val neighbours = mutableListOf<Point>()
    for ((dy, dx) in adjacent) {
        if (y + dy < 0 || y + dy == height || x + dx < 0 || x + dx == width) {
            continue
        }
        neighbours.add(Point(y + dy, x + dx))
    }
    return neighbours
}

data class Area(val x: IntRange, val y: IntRange) {
    operator fun contains(p: Point): Boolean {
        return p.x in x && p.y in y
    }

    companion object {
        fun fromPoints(image: Collection<Point>): Area {
            val minY = image.minOf { it.y }
            val maxY = image.maxOf { it.y }
            val minX = image.minOf { it.x }
            val maxX = image.maxOf { it.x }
            return Area(minX..maxX, minY..maxY)
        }
    }
}