package cz.tskopec.shiftscheduler.domain.builder.generator

//range of integers from nextCombination to limit, whose bit patterns have same number of 1s
class CombinationsRange(
	private var nextCombination: Int, // inclusive
	private val limit: Int // exclusive
) : Iterator<Int> {

	init {
		if(nextCombination == 0) throw IllegalStateException("Must not be 0")
	}


	override fun hasNext(): Boolean = nextCombination < limit

	override fun next(): Int {
		val result = nextCombination
		nextCombination = nextCombination(nextCombination)
		return result
	}


	//Gosper's hack
	private fun nextCombination(orig: Int): Int {

		// select rightmost bit in orig: (orig = 1001110) -> (c = 0000010)
		val c = orig and -orig
		// shift leftmost 1 of the rightmost 1s cluster to the left by 1 place, replace rest of the cluster with 0s:
		// (orig = 1001110) -> (r = 1010000)
		val r = orig + c
		// rest of the replaced cluster shifted all the way to the right(orig = 1001110) -> (q = 0000011)
		val q = ((r xor orig) shr 2) / c
		// return q and r combined
		return q or r
	}


	companion object {

		val EMPTY = CombinationsRange(1, 0)

		fun singleCombination(combination: Int): CombinationsRange
				= CombinationsRange(combination, combination + 1)
	}
}
