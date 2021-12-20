import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

enum class Rotation {
    DEG0, DEG90, DEG180, DEG270;

    val sin: Int
        get() = when (this) {
            DEG0   -> 0
            DEG90  -> 1
            DEG180 -> 0
            DEG270 -> -1
        }

    val cos: Int
        get() = when (this) {
            DEG0   -> 1
            DEG90  -> 0
            DEG180 -> -1
            DEG270 -> 0
        }
}

data class Orientation(val x: Rotation, val y: Rotation, val z: Rotation) {
    companion object {
        val ALL by lazy {
            val combinations = Rotation.values().toList()
                .cartesianProduct(
                    Rotation.values().toList()
                )
                .cartesianProduct(
                    Rotation.values().toList()
                )
                .map { (xy, z) ->
                    val (x, y) = xy
                    Orientation(x, y, z)
                }
            val rotated = mutableMapOf<Point3d, Orientation>()
            for (c in combinations) {
                rotated.computeIfAbsent(Point3d(1, 2, 3).orientate(c)) { c }
            }
            return@lazy rotated.values
        }

        val ZERO = Orientation(Rotation.DEG0, Rotation.DEG0, Rotation.DEG0)
    }
}

val Array<IntArray>.width: Int
    get() {
        return this.first().size
    }

val Array<IntArray>.height: Int
    get() {
        return this.size
    }

fun Array<IntArray>.mul(other: Point3d): Point3d {
    val product = IntArray(3)
    for (i in 0 until 3) {
        for (k in 0 until 3) {
            product[i] += this[i][k] * other[k]
        }
    }
    return Point3d(product[0], product[1], product[2])
}

fun Array<IntArray>.toPoint3d(): Point3d {
    require(width == 1 && height == 3)
    return Point3d(this[0][0], this[1][0], this[2][0])
}

data class Point3d(val x: Int, val y: Int, val z: Int) : Comparable<Point3d> {
    fun rotateX(rot: Rotation): Point3d {
        if (rot == Rotation.DEG0) return this.copy()
        val rotMat = arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(0, rot.cos, -rot.sin),
            intArrayOf(0, rot.sin, rot.cos)
        )
        return rotMat.mul(this)
    }

    fun rotateY(rot: Rotation): Point3d {
        if (rot == Rotation.DEG0) return this.copy()
        val rotMat = arrayOf(
            intArrayOf(rot.cos, 0, rot.sin),
            intArrayOf(0, 1, 0),
            intArrayOf(-rot.sin, 0, rot.cos)
        )
        return rotMat.mul(this)
    }

    fun rotateZ(rot: Rotation): Point3d {
        if (rot == Rotation.DEG0) return this.copy()
        val rotMat = arrayOf(
            intArrayOf(rot.cos, -rot.sin, 0),
            intArrayOf(rot.sin, rot.cos, 0),
            intArrayOf(0, 0, 1)
        )
        return rotMat.mul(this)
    }

    fun toIntArray(): Array<IntArray> {
        return arrayOf(intArrayOf(x), intArrayOf(y), intArrayOf(z))
    }

    operator fun plus(other: Point3d): Point3d {
        return Point3d(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Point3d): Point3d {
        return Point3d(x - other.x, y - other.y, z - other.z)
    }

    fun chebyshevDistance(other: Point3d): Int {
        return maxOf(abs(x - other.x), abs(y - other.y), abs(z - other.z))
    }

    fun manhattanDistance(other: Point3d): Int {
        return abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    }

    fun orientate(orientation: Orientation): Point3d {
        return this.rotateX(orientation.x).rotateY(orientation.y).rotateZ(orientation.z)
    }

    companion object {
        val ZERO = Point3d(0, 0, 0)
    }

    override fun compareTo(other: Point3d): Int {
        return compareValuesBy(this, other, { it.x }, { it.y }, { it.z })
    }

    operator fun get(idx: Int) = when (idx) {
        0    -> x
        1    -> y
        2    -> z
        else -> error("Out of scope")
    }
}

fun String.toPoint3d(): Point3d {
    val points = this.split(",")
    require(points.size == 3)
    return Point3d(points[0].toInt(), points[1].toInt(), points[2].toInt())
}

data class ScannerReport(val id: Int, val points: List<Point3d>)

data class Beacon(val position: Point3d) : Comparable<Beacon> {
    override fun compareTo(other: Beacon): Int {
        return this.position compareTo other.position
    }
}

data class Scanner(val report: ScannerReport, var position: Point3d? = null, var orientation: Orientation? = null) {
    fun getBeacons(): MutableList<Beacon> {
        val beacons = mutableListOf<Beacon>()
        require(this.position != null)
        require(this.orientation != null)
        for (point in report.points) {
            beacons.add(Beacon(position!! + point.orientate(orientation!!)))
        }
        return beacons
    }

    fun isOrientedWith(other: Scanner, requiredOverlappingBeacons: Int): Boolean {
        require(this.position != null)
        require(other.position != null)
        require(this.orientation != null)
        require(other.orientation != null)
        var overlapingBeacons = 0
        outer@ for (point1 in report.points) {
            for (point2 in other.report.points) {
                val beaconPos1 = point1.orientate(this.orientation!!) + this.position!!
                val beaconPos2 = point2.orientate(other.orientation!!) + other.position!!
                if (beaconPos1 == beaconPos2) {
                    overlapingBeacons += 1
                    if (overlapingBeacons == requiredOverlappingBeacons) {
                        return true
                    }
                    continue@outer
                }
            }
        }
        return false
    }

    fun orientate(other: Scanner, requiredOverlappingBeacons: Int = 12): Scanner? {
        require(position != null) { "This scanner is not oriented yet" }
        require(this.position != null)
        require(other.orientation == null)
        require(other.position == null)
        for (point1 in this.report.points.drop(requiredOverlappingBeacons - 1)) {
            for (point2 in other.report.points.drop(requiredOverlappingBeacons - 1)) {
                for (newOrientation in Orientation.ALL) {
                    val newPos = this.position!! + point1.orientate(this.orientation!!) - point2.orientate(newOrientation)
                    val newScanner = other.withPosition(newPos).withOrientation(newOrientation)
                    if (this.isOrientedWith(newScanner, requiredOverlappingBeacons)) {
                        return newScanner
                    }
                }
            }
        }
        return null
    }

    fun withPosition(pos: Point3d): Scanner {
        return this.copy().also { it.position = pos }
    }

    fun withOrientation(rot: Orientation): Scanner {
        return this.copy().also { it.orientation = rot }
    }

    private fun copy(): Scanner = Scanner(report, position?.copy(), orientation?.copy())
}

@ExperimentalTime
fun main() {
    fun parseInput(input: List<String>): List<Scanner> {
        val reports = mutableListOf<Scanner>()
        var currentPoints = mutableListOf<Point3d>()
        var currentReportId = 0
        for (line in input) {
            when {
                line.isEmpty()                -> {
                    reports.add(Scanner(ScannerReport(currentReportId, currentPoints)))
                    currentPoints = mutableListOf()
                }
                line.substring(0..2) == "---" -> {
                    val groups = "--- scanner (?<rId>\\d+) ---".toRegex().matchEntire(line)!!.groups as MatchNamedGroupCollection
                    currentReportId = groups["rId"]?.value?.toInt() ?: error("Wrong report header: $line")
                }
                else                          -> currentPoints.add(line.toPoint3d())
            }
        }
        reports.add(Scanner(ScannerReport(currentReportId, currentPoints)))
        return reports
    }

    fun orientateScanners(scanners: List<Scanner>): Set<Scanner> {
        val orientedScanners = mutableSetOf<Scanner>()
        var disorientedScanners = scanners.drop(1).toSet()
        orientedScanners.add(scanners.first().withOrientation(Orientation.ZERO).withPosition(Point3d.ZERO))
        outer@ while (disorientedScanners.isNotEmpty()) {
            for (disoriented in disorientedScanners) {
                for (oriented in orientedScanners) {
                    val newScanner = oriented.orientate(disoriented)
                    if (newScanner != null) {
                        println("Matched scanners ${orientedScanners.size}/${scanners.size} last matched: ${newScanner.report.id}")
                        orientedScanners.add(newScanner)
                        disorientedScanners = disorientedScanners.minus(disoriented)
                        continue@outer
                    }
                }
            }
            error("No new scanner oriented")
        }
        return orientedScanners
    }

    fun part1(scanners: Set<Scanner>): Int {
        require(scanners.none { it.position == null })
        require(scanners.none { it.orientation == null })
        return scanners.flatMap { it.getBeacons() }.toSet().size
    }

    fun part2(scanners: Set<Scanner>): Int {
        require(scanners.none { it.position == null })
        require(scanners.none { it.orientation == null })
        var maxDist = 0
        for (s1 in scanners) {
            for (s2 in scanners) {
                if (s1.report.id == s2.report.id) {
                    continue
                }
                maxDist = maxOf(s1.position!!.manhattanDistance(s2.position!!), maxDist)
            }
        }
        return maxDist
    }

    val dayId = "19"
    check(Point3d(5, 4, 2).rotateX(Rotation.DEG90) == Point3d(5, -2, 4))
    check(Point3d(5, 4, 2).rotateX(Rotation.DEG90).rotateX(Rotation.DEG90).rotateX(Rotation.DEG180) == Point3d(5, 4, 2))
    val smallInput = parseInput(readInput("Day${dayId}_small"))
    check(
        smallInput == listOf(
            Scanner(ScannerReport(0, listOf(Point3d(0, 2, 0), Point3d(4, 1, 0), Point3d(3, 3, 0)))),
            Scanner(ScannerReport(1, listOf(Point3d(-1, -1, 0), Point3d(-5, 0, 0), Point3d(-2, 1, 0))))
        )
    ) { "$smallInput not parsed correctly" }
    check(Point3d(-1, -1, -1).chebyshevDistance(Point3d(3, 5, 33)) == 34)
    check(
        Scanner(ScannerReport(0, listOf(Point3d(-1, -1, -1))))
            .withPosition(Point3d(-2, -2, -2)).position == Point3d(-2, -2, -2)
    )
    check(
        Scanner(ScannerReport(0, listOf(Point3d(15, 4, 0))))
            .withPosition(Point3d(4, -15, 0))
            .withOrientation(Orientation(Rotation.DEG0, Rotation.DEG0, Rotation.DEG90))
            .isOrientedWith(
                Scanner(ScannerReport(1, listOf(Point3d(6, -2, 0))))
                    .withPosition(Point3d(-2, -6, 0))
                    .withOrientation(Orientation(Rotation.DEG0, Rotation.DEG0, Rotation.DEG90)),
                requiredOverlappingBeacons = 1
            )
    )
    check(
        Scanner(ScannerReport(0, listOf(Point3d(15, 4, 0))))
            .withPosition(Point3d(4, -15, 0))
            .withOrientation(Orientation(Rotation.DEG0, Rotation.DEG0, Rotation.DEG90))
            .orientate(
                Scanner(ScannerReport(1, listOf(Point3d(6, -2, 0)))),
                requiredOverlappingBeacons = 1
            )!!.position == Point3d(-6, 2, 0)
    )
    check(Point3d(1105, -1205, 1229).manhattanDistance(Point3d(-92, -2380, -20)) == 3621)
    val duration = measureTime {
        check(Orientation.ALL.size == 24)
        val testInput = parseInput(readInput("Day${dayId}_test"))
        val testOrientatedScanners = orientateScanners(testInput)
        check(part1(testOrientatedScanners) == 79)
        check(part2(testOrientatedScanners) == 3621)

        val input = parseInput(readInput("Day${dayId}"))
        val orientatedScanners = orientateScanners(input)
        println(part1(orientatedScanners))
        println(part2(orientatedScanners))
    }
    println(duration)
}