import java.util.Stack
import kotlin.math.ceil
import kotlin.math.floor

sealed class SnailfishNumber : Iterable<SnailfishNumber> {
    operator fun plus(other: SnailfishNumber): SnailfishNumber {
        val result = Complex(this, other)
        return result.reduce()
    }

    var parent: SnailfishNumber? = null
    abstract var left: SnailfishNumber?
    abstract var right: SnailfishNumber?

    abstract fun height(): Int
    abstract fun maxNumber(): Int
    abstract fun first(): Simple
    abstract fun last(): Simple
    abstract fun magnitude(): Int
    abstract fun copy(): SnailfishNumber

    @Suppress("DuplicatedCode")
    fun successor(): SnailfishNumber? {
        if (this.right != null) {
            return this.right!!.first()
        }
        var r: SnailfishNumber? = this.parent
        var p: SnailfishNumber? = this
        while (r != null && p === r.right) {
            p = r
            r = r.parent
        }
        return r
    }

    @Suppress("DuplicatedCode")
    fun predecessor(): SnailfishNumber? {
        if (this.left != null) {
            return this.left!!.last()
        }
        var r: SnailfishNumber? = this.parent
        var p: SnailfishNumber? = this
        while (r != null && p === r.left) {
            p = r
            r = r.parent
        }
        return r
    }

    fun shouldExplode() = height() > EXPLODE_LEVEL
    fun shouldSplit() = maxNumber() >= 10

    fun reduce(): SnailfishNumber {
        val after = when {
            shouldExplode() -> explode()
            shouldSplit()   -> split()
            else            -> return this
        }
        return after.reduce()
    }

    fun depth(): Int {
        var p: SnailfishNumber = this
        var depth = 0
        while (p.parent != null) {
            depth++
            p = p.parent!!
        }
        return depth
    }

    fun root(): SnailfishNumber {
        var p: SnailfishNumber = this
        while (p.parent != null) {
            p = p.parent!!
        }
        return p
    }

    override fun iterator(): Iterator<SnailfishNumber> = object : Iterator<SnailfishNumber> {
        var current: SnailfishNumber? = this@SnailfishNumber.first()

        override fun hasNext(): Boolean = current != null

        override fun next(): SnailfishNumber {
            val next = current!!
            current = current?.successor()
            return next
        }
    }

    fun explode(): SnailfishNumber {
        val tooNested = this.filterIsInstance<Complex>().first { it.depth() >= EXPLODE_LEVEL }
        val succ = tooNested.right!!.successor()
        val pred = tooNested.left!!.predecessor()
        (tooNested.right as Simple).value.let {
            succ?.right?.first()?.apply { value += it }
        }
        (tooNested.left as Simple).value.let {
            pred?.left?.last()?.apply { value += it }
        }
        return tooNested.replace(Simple(0))
    }

    fun replace(new: SnailfishNumber): SnailfishNumber {
        when {
            this.parent == null         -> return new
            this.parent?.right === this -> this.parent?.right = new
            this.parent?.left === this  -> this.parent?.left = new
        }
        new.parent = this.parent
        val root = root()
        this.parent = null // disconnect fom parent
        return root
    }

    fun split(): SnailfishNumber {
        val tooBig: Simple = this.mapNotNull { (it as? Simple) }.first { it.value >= 10 }
        val value = tooBig.value
        return tooBig.replace(Complex(Simple(floor(value / 2.0).toInt()), Simple(ceil(value / 2.0).toInt())))
    }

    companion object {
        const val EXPLODE_LEVEL = 4
    }
}

data class Simple(var value: Int) : SnailfishNumber() {
    override var left: SnailfishNumber? = null
    override var right: SnailfishNumber? = null
    override fun height() = 0
    override fun maxNumber(): Int = value
    override fun first(): Simple = this
    override fun last(): Simple = this
    override fun toString() = value.toString()
    override fun magnitude(): Int = value
    override fun copy() = Simple(value)
}

data class Complex(override var left: SnailfishNumber?, override var right: SnailfishNumber?) : SnailfishNumber() {

    init {
        left?.parent = this
        right?.parent = this
    }

    override fun height(): Int = 1 + maxOf(left!!.height(), right!!.height())
    override fun maxNumber(): Int = maxOf(left!!.maxNumber(), right!!.maxNumber())
    override fun first(): Simple {
        var current: SnailfishNumber = this
        while (current !is Simple) {
            current = (current as Complex).left!!
        }
        return current
    }

    override fun last(): Simple {
        var current: SnailfishNumber = this
        while (current !is Simple) {
            current = (current as Complex).right!!
        }
        return current
    }

    override fun magnitude(): Int {
        return 3 * left!!.magnitude() + 2 * right!!.magnitude()
    }

    override fun copy() = Complex(left!!.copy(), right!!.copy())

    override fun toString() = "[$left,$right]"
}

sealed class Token
data class Number(val num: Int) : Token()
object LBracket : Token()
object RBracket : Token()
data class Parsed(val number: SnailfishNumber) : Token()

fun main() {
    fun String.toSFN(): SnailfishNumber {
        val tokenized = this.replace(",", " ")
            .replace("[", "[ ")
            .replace("]", " ]")
            .split(" ")
            .map {
                when (it) {
                    "["  -> LBracket
                    "]"  -> RBracket
                    else -> Number(it.toInt())
                }
            }
        val stack = Stack<Token>()

        for (symbol in tokenized) {
            when (symbol) {
                is LBracket -> stack.add(LBracket)
                is Number   -> stack.add(Parsed(Simple(symbol.num)))
                is RBracket -> {
                    val second = stack.pop() as Parsed
                    val first = stack.pop() as Parsed
                    stack.pop() as LBracket
                    val parsed = Parsed(Complex(first.number, second.number))
                    stack.add(parsed)
                }
                else        -> error("Unknown symbol: $symbol")
            }
        }
        return (stack.single() as Parsed).number
    }

    fun parseInput(input: List<String>): List<SnailfishNumber> {
        return input.map { it.toSFN() }
    }

    fun part1(numbers: List<SnailfishNumber>): Int {
        return numbers.reduce { acc, sfn -> acc + sfn }.magnitude()
    }

    fun part2(numbers: List<SnailfishNumber>): Int {
        var ans = Int.MIN_VALUE
        for (num1 in numbers) {
            for (num2 in numbers) {
                if (num1 === num2) {
                    continue
                }
                ans = maxOf(ans, (num1.copy() + num2.copy()).magnitude())
            }
        }
        for (num1 in numbers) {
            for (num2 in numbers) {
                if (num1 === num2) {
                    continue
                }
                ans = maxOf(ans, (num2.copy() + num1.copy()).magnitude())
            }
        }
        return ans
    }

    val dayId = "18"

    // test if implementation meets criteria from the description, like:
    check("[1,2]".toSFN() == Complex(Simple(1), Simple(2)))
    check(
        "[[[[1,2],[3,4]],[[5,6],[7,8]]],9]".toSFN() ==
                Complex(
                    Complex(
                        Complex(
                            Complex(
                                Simple(1), Simple(2)
                            ),
                            Complex(
                                Simple(3), Simple(4)
                            )
                        ),
                        Complex(
                            Complex(
                                Simple(5), Simple(6)
                            ),
                            Complex(
                                Simple(7), Simple(8)
                            )
                        )
                    ), Simple(9)
                )
    )
    check(!"[[[[1,2],[3,4]],[[5,6],[7,8]]],9]".toSFN().shouldExplode())
    check("[[[[[9,8],1],2],3],4]".toSFN().shouldExplode())
    check(!"[1,4]".toSFN().shouldSplit())
    check("[21,37]".toSFN().shouldSplit())
    check("[1,2]".toSFN() + "[[3,4],5]".toSFN() == "[[1,2],[[3,4],5]]".toSFN())
    check("[[[[[9,8],1],2],3],4]".toSFN().last().value == 4)
    check("[[[[[9,8],1],2],3],4]".toSFN().first().value == 9)
    check(("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]".toSFN().left!!.right!!.right!!.right!!.left!! as Simple).value == 7)
    check(
        "[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]".toSFN().left!!.right!!.right!!.right!!.left!!
            .predecessor()!!.left!!.last().value == 1
    )
    check(("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]".toSFN().left!!.right!!.right!!.right!!.right!! as Simple).value == 3)
    check(
        "[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]".toSFN().left!!.right!!.right!!.right!!.right!!
            .successor()!!.right!!.first().value == 6
    )
    check("[[[[[9,8],1],2],3],4]".toSFN().first().depth() == 5)
    check("[[[[[9,8],1],2],3],4]".toSFN().first().parent!!.replace(Simple(42)).toString() == "[[[[42,1],2],3],4]")
    check("[[[[[9,8],1],2],3],4]".toSFN().explode() == "[[[[0,9],2],3],4]".toSFN())
    check("[[6,[5,[4,[3,2]]]],1]".toSFN().explode() == "[[6,[5,[7,0]]],3]".toSFN())
    check("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]".toSFN().explode() == "[[3,[2,[8,0]]],[9,[5,[7,0]]]]".toSFN())
    check("10".toSFN().split() == "[5,5]".toSFN())
    check("11".toSFN().split() == "[5,6]".toSFN())
    check("11".toSFN().height() == 0)
    check("[1,2]".toSFN().height() == 1)
    check("[[1,2],3]".toSFN().height() == 2)
    check("[[[1,2],3],4]".toSFN().height() == 3)
    check("[[[[1,2],3],4],5]".toSFN().height() == 4)
    check("[[[[[1,2],3],4],5],6]".toSFN().height() == 5)
    check("[1,2]".toSFN().depth() == 0)
    check("[[1,2],3]".toSFN().left!!.depth() == 1)
    check(("[[[[[1,2],3],4],5],6]".toSFN().left!!.left!!.left!!.left!!.left!! as Simple).value == 1)
    check("[[[[[1,2],3],4],5],6]".toSFN().left!!.left!!.left!!.left!!.depth() == 4)
    check(("[[[[4,3],4],4],[7,[[8,4],9]]]".toSFN() + "[1,1]".toSFN()).reduce() == "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]".toSFN())
    check(listOf("[1,1]", "[2,2]", "[3,3]", "[4,4]", "[5,5]").map { it.toSFN() }
        .reduce { acc, it -> acc + it } == "[[[[3,0],[5,3]],[4,4]],[5,5]]".toSFN())
    check("[[1,2],[[3,4],5]]".toSFN().magnitude() == 143)
    check("[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]".toSFN().magnitude() == 3488)
    val testInput = parseInput(readInput("Day${dayId}_test"))
    check(part1(testInput.map { it.copy() }) == 4140)
    check(part2(testInput.map { it.copy() }) == 3993)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input.map { it.copy() }))
    println(part2(input.map { it.copy() }))
}