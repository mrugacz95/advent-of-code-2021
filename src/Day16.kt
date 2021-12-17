class BitBuffer(initBits: String) : AbstractList<String>() {
    private var bits = initBits

    fun read(n: Int): String {
        val taken = bits.take(n)
        bits = bits.drop(n)
        return taken
    }

    override val size: Int
        get() = bits.length

    override fun get(index: Int): String = bits[index].toString()
}

class Packet(private val bits: BitBuffer) {

    private val version = bits.read(3).toInt(2)
    private val typeId = bits.read(3).toInt(2)
    var value: Long? = null
        private set
    val includedPackets = mutableListOf<Packet>()

    init {
        when (typeId) {
            4    -> readLiteralPacket()
            else -> readOperatorPacket()
        }
    }

    private fun readLiteralPacket() {
        var number = ""
        do {
            val lastGroup = bits.read(1)
            number += bits.read(4)
        } while (lastGroup != "0")
        value = number.toLong(2)
    }

    private fun readOperatorPacket() {

        when (bits.read(1).toInt(2)) {  // length type ID
            0 -> {
                val bitsOfSubPackets = bits.read(15).toInt(2)
                val subPackets = BitBuffer(bits.read(bitsOfSubPackets))
                while (subPackets.isNotEmpty()) {
                    includedPackets += Packet(subPackets)
                }
            }
            1 -> {
                val numberOfSubPackets = bits.read(11).toInt(2)
                for (i in 1..numberOfSubPackets) {
                    includedPackets += Packet(bits)
                }
            }
        }
        value = when (typeId) {
            0    -> { // sum
                includedPackets.sumOf { it.value!! }
            }
            1    -> { // mul
                includedPackets.map { it.value }.reduce { acc, value -> acc!! * value!! }
            }
            2    -> { // min
                includedPackets.minOf { it.value!! }
            }
            3    -> { // max
                includedPackets.maxOf { it.value!! }
            }
            5    -> { // gt
                if (includedPackets[0].value!! > includedPackets[1].value!!) {
                    1
                } else {
                    0
                }
            }
            6    -> { // ls
                if (includedPackets[0].value!! < includedPackets[1].value!!) {
                    1
                } else {
                    0
                }
            }
            7    -> { // eq
                if (includedPackets[0].value!! == includedPackets[1].value!!) {
                    1
                } else {
                    0
                }
            }
            else -> error("Unknown type: $typeId")
        }
    }

    fun sumVersions(): Int {
        return version + includedPackets.sumOf { it.sumVersions() }
    }
}

fun main() {
    fun parseInput(input: List<String>): String {
        return input.single()
            .map {
                it.toString()
                    .toInt(16)
                    .toString(2)
                    .padStart(4, '0')
            }
            .joinToString("")
    }

    fun part1(input: String): Int {
        return Packet(BitBuffer(input)).sumVersions()
    }

    fun part2(input: String): Long {
        return Packet(BitBuffer(input)).value!!
    }

    val dayId = "16"
    val literalPacket = parseInput(listOf("D2FE28"))
    check(literalPacket == "110100101111111000101000")
    check(Packet(BitBuffer(literalPacket)).value == 2021L)

    val operatorPacketIdZero = parseInput(listOf("38006F45291200"))
    check(operatorPacketIdZero == "00111000000000000110111101000101001010010001001000000000")
    check(Packet(BitBuffer(operatorPacketIdZero)).includedPackets.size == 2)
    val operatorPacketIdOne = parseInput(listOf("EE00D40C823060"))
    check(operatorPacketIdOne == "11101110000000001101010000001100100000100011000001100000")
    check(Packet(BitBuffer(operatorPacketIdOne)).includedPackets.size == 3)
    // test if implementation meets criteria from the description, like:
    val partOneTestInput = parseInput(listOf("8A004A801A8002F478"))
    check(part1(partOneTestInput) == 16)
    val partTwoTestInput = parseInput(listOf("9C0141080250320F1802104A08"))
    check(part2(partTwoTestInput) == 1L)

    val input = parseInput(readInput("Day${dayId}"))
    println(part1(input))
    println(part2(input))
}