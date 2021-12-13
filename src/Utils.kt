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
data class Point(val y: Int, val x: Int)

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