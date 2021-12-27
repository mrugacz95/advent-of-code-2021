import kotlin.math.abs

fun IntRange.intersect(other: IntRange): Boolean =
    (this.first <= other.last && this.first >= other.first) || (other.first <= this.last && other.last >= this.first)

fun IntRange.isInside(other: IntRange): Boolean = this.first >= other.first && this.last <= other.last

fun IntRange.contains(other: IntRange): Boolean {
    return other.first <= other.last && other.first >= this.first && other.last <= this.last
}

fun IntRange.chooseSplitPlane(other: IntRange): Int? {
    if (!this.intersect(other)) {
        return null
    }
    if (this == other) {
        return null
    }
    if (other.first != this.first && other.first in this) {
        return other.first - 1
    }
    if (other.last != this.last && other.last in this) {
        return other.last
    }
    if (other.first != this.first && this.first in other) {
        return this.first - 1
    }
    if (other.last != this.last && this.last in this) {
        return this.last
    }
    error("Can't find split for $this and $other")
}

fun IntRange.overlap(other: IntRange): Int {
    if (this.isInside(other)) {
        return this.length()
    }
    if (other.isInside(this)) {
        return other.length()
    }
    return maxOf(0, minOf(this.last, other.last) - maxOf(this.first, other.first))
}

fun IntRange.length(): Int = last - first + 1

data class Cuboid(val x: IntRange, val y: IntRange, val z: IntRange) {
    fun intersect(other: Cuboid?): Boolean {
        if (other == null) return false
        if (this.x.intersect(other.x) &&
            this.y.intersect(other.y) &&
            this.z.intersect(other.z)
        ) {
            return true
        }
        return false
    }

    fun intersectionVolume(other: Cuboid): Long {
        if (!this.intersect(other)) return 0
        val xOverlap = this.x.overlap(other.x).toLong()
        val yOverlap = this.y.overlap(other.y).toLong()
        val zOverlap = this.z.overlap(other.z).toLong()
        return xOverlap * yOverlap * zOverlap
    }

    fun volume(): Long {
        return x.length().toLong() * y.length().toLong() * z.length().toLong()
    }

    fun split(plane: HyperPlane): Pair<Cuboid?, Cuboid?> {
        if (plane.x != null) {
            if (plane.x < x.first) {
                return Pair(null, this)
            }
            if (plane.x >= x.last) {
                return Pair(this, null)
            }
            return Pair(
                first = copy(x = x.first..plane.x, y = y, z = z),
                second = copy(x = (plane.x + 1)..x.last, y = y, z = z)
            )
        }
        if (plane.y != null) {
            if (plane.y < y.first) {
                return Pair(null, this)
            }
            if (plane.y >= y.last) {
                return Pair(this, null)
            }
            return Pair(
                first = copy(x = x, y = y.first..plane.y, z = z),
                second = copy(x = x, y = ((plane.y + 1)..y.last), z = z)
            )
        }
        if (plane.z != null) {
            if (plane.z < z.first) {
                return Pair(null, this)
            }
            if (plane.z >= z.last) {
                return Pair(this, null)
            }
            return Pair(
                first = copy(x = x, y = y, z = z.first..plane.z),
                second = copy(x = x, y = y, z = ((plane.z + 1)..z.last))
            )
        }
        error("This hyperplane doesn't split cuboid")
    }

    fun center() = Point3d((x.first + x.last) / 2, (y.first + y.last) / 2, (z.first + z.last) / 2)

    fun distance(plane: HyperPlane): Int {
        val center = center()
        if (plane.x != null) {
            return abs(plane.x - center.x)
        }
        if (plane.y != null) {
            return abs(plane.y - center.y)
        }
        require(plane.z != null)
        return abs(plane.z - center.z)
    }

    fun getAxes(): Set<HyperPlane> {
        return setOf(
            HyperPlane(x = x.first - 1),
            HyperPlane(x = x.last),
            HyperPlane(y = y.first - 1),
            HyperPlane(y = y.last),
            HyperPlane(z = z.first - 1),
            HyperPlane(z = z.last),
        )
    }

    operator fun contains(other: Cuboid): Boolean {
        return this.x.contains(other.x) && this.y.contains(other.y) && this.z.contains(other.z)
    }

    operator fun contains(p3: Point3d): Boolean {
        return p3.x in this.x && p3.y in this.y && p3.z in this.z
    }

    fun add(other: Cuboid): Cuboid {
        return Cuboid(
            minOf(x.first, other.x.first)..maxOf(x.last, other.x.last),
            minOf(y.first, other.y.first)..maxOf(y.last, other.y.last),
            minOf(z.first, other.z.first)..maxOf(z.last, other.z.last),
        )
    }
}

data class HyperPlane(val x: Int? = null, val y: Int? = null, val z: Int? = null) {
    init {
        check(listOfNotNull(x, y, z).size == 1)
    }

    fun getSide(p3: Point3d): Boolean {
        if (x != null) {
            return p3.x <= x
        }
        if (y != null) {
            return p3.y <= y
        }
        require(z != null)
        return p3.z <= z
    }
}

class BSPTree(private val volume: Cuboid) {
    private var state: Boolean? = null
    private var cuboid: Cuboid? = null
    private var subtrees: Pair<BSPTree, BSPTree>? = null
    private var splittingPlane: HyperPlane? = null

    fun add(cuboidToAdd: Cuboid, stateToAdd: Boolean) {
        println("Add $cuboidToAdd into $volume containing $cuboid")
        require(cuboidToAdd in volume)
        if (splittingPlane != null && subtrees != null) {
            val (c1, c2) = cuboidToAdd.split(splittingPlane!!)
            println("Node already splitted propagate cuboid: $c1 $c2")
            if (c1 != null) {
                subtrees!!.first.add(c1, stateToAdd)
            }
            if (c2 != null) {
                subtrees!!.second.add(c2, stateToAdd)
            }
            return
        }
        if (cuboid == null) {
            cuboid = cuboidToAdd
            state = stateToAdd
            println("Cuboid set $cuboid in node: $volume")
            return
        } else if (cuboidToAdd == cuboid) {
            state = stateToAdd
            return // skip for now
        }
        val plane = otherChooseSplitAxis(cuboidToAdd, cuboid!!)
        val (c1, c2) = volume.split(plane)
        println("split needed: $volume into $c1 $c2 with $plane")
        val bsptree1 = BSPTree(c1!!)
        val bsptree2 = BSPTree(c2!!)
        for ((obj, objState) in listOf(
            Pair(cuboid, state!!),
            Pair(cuboidToAdd, stateToAdd)
        )) {
            val (o1, o2) = obj!!.split(plane)
            if (o1 != null) {
                bsptree1.add(o1, objState)
//                println("bsp tree 1 after add:")
//                println(bsptree1.toString(0, 0))
            }
            if (o2 != null) {
                bsptree2.add(o2, objState)
//                println("bsp tree 2 after add:")
//                println(bsptree2.toString(0, 0))
            }
        }
        state = null
        cuboid = null
        subtrees = Pair(bsptree1, bsptree2)
        splittingPlane = plane
    }

    private fun otherChooseSplitAxis(c1: Cuboid, c2: Cuboid): HyperPlane {
        val outerAxis = c1.add(c2).getAxes()
        val c1Axes = c1.getAxes().filter { it !in outerAxis }
        val c2Axes = c2.getAxes().filter { it !in outerAxis }
        val best = (c1Axes + c2Axes).first()
        println("best $c1 and $c2 plane is $best")
        return best
    }

    private fun countNodes(): Int {
        if (subtrees != null) {
            return subtrees!!.first.countNodes() + subtrees!!.second.countNodes()
        }
        return 1
    }

    fun toString(z: Int, id: Int, arr: Array<IntArray>? = null): String {
        val backgrounds = listOf('.', ',', '"', ';', ':', '-', '+')
        if (subtrees != null) {
            val rightId = id + subtrees!!.first.countNodes() + 2
//            println("id: $id, c: $cuboid, v: $volume, first: ${id+1}, second: $rightId")
            val sub1 = subtrees!!.first.toString(z, id + 1, arr)
            val sub2 = subtrees!!.second.toString(z, rightId, arr)

            return if (splittingPlane!!.x != null) { // x join
                sub1.split("\n").zip(sub2.split("\n")) { s1, s2 -> "$s1$s2" }.joinToString("\n")
            } else { // y join
                sub1 + sub2
            }
        }
        val background = backgrounds[id % backgrounds.size]
        val result = Array(volume.y.length()) { Array(volume.x.length()) { background } }
        if (state == true) {
            if (cuboid != null) {
                for (y in cuboid!!.y) {
                    for (x in cuboid!!.x) {
                        result[y - volume.y.first][x - volume.x.first] = id.toString().last()
                    }
                }
            }
        }
        return result.joinToString("\n") { row -> row.joinToString("") { it.toString() } } + "\n"
    }

    fun countBits(): Long {
        if (splittingPlane == null) {
            return state!!.toInt() * cuboid!!.volume()
        }
        return subtrees?.first!!.countBits() + subtrees?.second!!.countBits()
    }
}

fun main() {
    fun parseInput(input: List<String>): List<Pair<Boolean, Cuboid>> {
        return input.map {
            val groups =
                "(?<state>on|off) x=(?<xStart>-?\\d+)..(?<xEnd>-?\\d+),y=(?<yStart>-?\\d+)..(?<yEnd>-?\\d+),z=(?<zStart>-?\\d+)..(?<zEnd>-?\\d+)".toRegex()
                    .matchEntire(it)!!.groups
            val state = groups["state"]!!.value == "on"
            Pair(
                state,
                Cuboid(
                    groups["xStart"]!!.value.toInt()..groups["xEnd"]!!.value.toInt(),
                    groups["yStart"]!!.value.toInt()..groups["yEnd"]!!.value.toInt(),
                    groups["zStart"]!!.value.toInt()..groups["zEnd"]!!.value.toInt()
                )
            )
        }
    }

    fun mark(reactor: MutableMap<Triple<Int, Int, Int>, Int>, value: Int, cuboid: Cuboid) {
        for (z in cuboid.z)
            for (y in cuboid.y)
                for (x in cuboid.x)
                    reactor[Triple(z, y, x)] = value
    }

    fun part1(steps: List<Pair<Boolean, Cuboid>>): Int {
        val size = 50
        val reactor = HashMap<Triple<Int, Int, Int>, Int>()
        for ((state, step) in steps) {
            if (step.x.first >= -size && step.x.last <= size &&
                step.y.first >= -size && step.y.last <= size &&
                step.z.first >= -size && step.z.last <= size
            )
                mark(reactor, state.toInt(), step)
        }
        return reactor.values.count { it == 1 }
    }

    fun calcRootSize(steps: List<Pair<Boolean, Cuboid>>): Cuboid {
        val xMin = steps.minOf { it.second.x.first }
        val xMax = steps.maxOf { it.second.x.last }
        val yMin = steps.minOf { it.second.y.first }
        val yMax = steps.maxOf { it.second.y.last }
        val zMin = steps.minOf { it.second.z.first }
        val zMax = steps.maxOf { it.second.z.last }
        return Cuboid(xMin..xMax, yMin..yMax, zMin..zMax)
    }

    fun part2(steps: List<Pair<Boolean, Cuboid>>): Long {
        val root = calcRootSize(steps)
        val tree = BSPTree(root)
        val size = 50
        for ((state, step) in steps) {
//            if (step.x.first >= -size && step.x.last <= size &&
//                step.y.first >= -size && step.y.last <= size &&
//                step.z.first >= -size && step.z.last <= size
//            ) {
            tree.add(step, state)
//            }
        }
        return tree.countBits()
    }

    val dayId = "22"

    // test if implementation meets criteria from the description, like:

    val root = BSPTree(Cuboid(0..10, 0..10, 0..0))
    println(root.toString(0, 0))
    root.add(Cuboid(1..3, 2..4, 0..0), true)
    println(root.toString(0, 0))
    root.add(Cuboid(4..7, 2..4, 0..0), true)
    println(root.toString(0, 0))
    root.add(Cuboid(4..7, 3..8, 0..0), true)
    println(root.toString(0, 0))
    root.add(Cuboid(7..8, 3..4, 0..0), true)
    println(root.toString(0, 0))
    root.add(Cuboid(5..6, 7..8, 0..0), false)
    println(root.toString(0, 0))
//    println(root.toString(0, 0))

//    val b = part2(testInput)
//    check((0..5).intersect(4..7))
//    check((5..8).intersect(1..5))
//    check(!(1..2).intersect(3..4))
//    check((0..5).overlap(2..8) == 3)
//    check((0..5).overlap(11..15) == 0)
//    check(Cuboid(2..4, 5..7, 4..6).volume() == 27L)

    val testInput1 = parseInput(readInput("Day${dayId}_test1"))
    check(part1(testInput1) == 590784)

    val testInput2 = parseInput(readInput("Day${dayId}_test2"))
    check(part2(testInput2) == 2758514936282235L)
//
    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}