package cz.tskopec.shiftscheduler.domain.entities

enum class ShiftType(
	val symbol: Char
) {

	DAY_OFF(' '),
	DAY('D'),
	NIGHT('N'),
	MORNING('M');

	fun next() = values()[(this.ordinal + 1) % values().size]
	fun isLastWorkType(): Boolean = this == workShiftTypes.last()


	companion object{

		val workShiftTypes = values().filterNot { it == DAY_OFF }
		val allSymbols = values().joinToString(separator = "") { it.symbol.toString() }

		fun getWorkShiftBySymbol(symbol: Char?): ShiftType? =
			if(symbol != null) workShiftTypes.firstOrNull { symbol.equals(it.symbol, true) } else null

	}
}


