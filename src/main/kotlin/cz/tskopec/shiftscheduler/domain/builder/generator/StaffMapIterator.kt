package cz.tskopec.shiftscheduler.domain.builder.generator

import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import kotlin.random.Random

// all possible employee combinations of requiredStaffSize, taken from availableStaff.
class StaffMapIterator (
	private val availableStaffMap: StaffMap,
	requiredStaffSize: Int
) : Iterator<StaffMap> {
	/*
	Complete range of possible staff combinations is divided into two at a random point. The subrange starting at this point
	is iterated first to ensure different results over multiple runs of the generator.
	 */
	private val ranges: MutableList<CombinationsRange>
	private var currentRange: CombinationsRange

	init {

		ranges = when {
			availableStaffMap.employeeCount < requiredStaffSize -> {
				mutableListOf(CombinationsRange.EMPTY)
			}
			availableStaffMap.employeeCount == requiredStaffSize -> {
				mutableListOf(CombinationsRange.singleCombination((1 shl requiredStaffSize) - 1))
			}
			else -> {
				// find first and last combination, and a random midpoint between them to create two ranges.
				val firstComb = (1 shl requiredStaffSize) - 1
				val midComb = randomCombination(availableStaffMap.employeeCount, requiredStaffSize)
				val lastComb = (firstComb shl (availableStaffMap.employeeCount - requiredStaffSize))

				listOf(
					CombinationsRange(firstComb, midComb),
					CombinationsRange(midComb, lastComb + 1)
				).filter { it.hasNext() }.toMutableList()
			}
		}
		currentRange = ranges.last()
	}


	override fun hasNext(): Boolean = currentRange.hasNext()

	override fun next(): StaffMap {

		val combination = currentRange.next()
		val selectedStaff = selectFromStaff(combination)
		if (!currentRange.hasNext()) {
			ranges.remove(currentRange)
			currentRange = if (ranges.isEmpty()) CombinationsRange.EMPTY else ranges.last()
		}
		return selectedStaff
	}


	// number with k 1-bits within the lowest n bits
	private fun randomCombination(n: Int, k: Int): Int {

		return generateSequence { Random.nextInt(n) }
			.distinct()
			.take(k)
			.fold(0) { a, b -> a or (1 shl b) }
	}

	// For every n-th 1-bit in combination select n-th employee from available staff
	private fun selectFromStaff(combination: Int): StaffMap {

		var availableStaffBits = availableStaffMap.bits
		var selectionBitMap = combination
		var selectedStaffBits = 0

		while (selectionBitMap > 0) {
			val selectedEmployee = availableStaffBits and -availableStaffBits //selects rightmost bit
			if ((selectionBitMap and 1) != 0)
				selectedStaffBits = selectedStaffBits or selectedEmployee
			availableStaffBits = availableStaffBits xor selectedEmployee
			selectionBitMap = selectionBitMap shr 1
		}
		return StaffMap(selectedStaffBits)
	}


	companion object {

		val EMPTY = object: Iterator<StaffMap> {
			override fun hasNext(): Boolean = false
			override fun next(): StaffMap = throw UnsupportedOperationException()
		}

		fun ofSingleMap(map: StaffMap): Iterator<StaffMap> {
			return listOf(map).iterator()
		}
	}
}
