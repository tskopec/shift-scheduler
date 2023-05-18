package cz.tskopec.shiftscheduler

import cz.tskopec.shiftscheduler.domain.entities.Schedule.Companion.MAX_STAFF_SIZE
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.startup.MainMenu
import java.io.File
import java.util.*

object Settings {

	private val properties = Properties().apply {
		load(File(MainMenu.settingsPath).inputStream())
	}

	val defaultStaffSize = readIntProp("default-staff-size", 6, 1 .. MAX_STAFF_SIZE)
	val month: String = properties["month"] as? String ?: ""
	val defaultRequirements = readRequirements(properties["requirements"] as? String)
	val rowLimit = readIntProp("row-limit", 3, 1 .. 31)
	val illegalOrderings = readTypeOrderings((properties["illegal-orders"] as? String))



	private fun readIntProp(key: String, defValue: Int, valid: IntRange): Int {

		return maxOf(valid.first, minOf(valid.last, (properties[key] as? String)?.toIntOrNull() ?: defValue))
	}


	private fun readRequirements(str: String?): Map<ShiftType, Int> {

		return readPairs(str, Regexes.SHIFT_TYPE, Regexes.NUMBER).mapNotNull { (s1, s2) ->
			val type = ShiftType.getWorkShiftBySymbol(s1.first()) ?: return@mapNotNull null
			val count = s2.toIntOrNull() ?: return@mapNotNull null
			type to count
		}.toMap().toMutableMap().apply {
			ShiftType.workShiftTypes.forEach { type -> this.computeIfAbsent(type) { 1 } }
		}
	}

	private fun readTypeOrderings(str: String?): List<Pair<ShiftType, ShiftType>> {

		return readPairs(str, Regexes.SHIFT_TYPE, Regexes.SHIFT_TYPE).mapNotNull { (s1, s2) ->
			val type1 = ShiftType.getWorkShiftBySymbol(s1.first()) ?: return@mapNotNull null
			val type2 = ShiftType.getWorkShiftBySymbol(s2.first()) ?: return@mapNotNull null
			type1 to type2
		}
	}

	private fun readPairs(str: String?, p1: Regex, p2: Regex): List<Pair<String, String>> {

		return str?.split(Regexes.SEPARATOR)
			?.map { it.replace(Regexes.WHITESPACE, "") }
			?.mapNotNull { s ->
				val regex = Regex("^($p1)($p2)$", RegexOption.IGNORE_CASE)
				val (group1, group2) = regex.matchEntire(s)?.destructured ?: return@mapNotNull null
				group1 to group2
			} ?: emptyList()
	}

	object Regexes {

		val SHIFT_TYPE = Regex("[${ShiftType.allSymbols}]")
		val NUMBER = Regex("\\d+")
		val WHITESPACE = Regex("\\s+")
		val SEPARATOR = Regex(",")
	}

}