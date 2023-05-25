package cz.tskopec.shiftscheduler.domain.builder.generator

import cz.tskopec.shiftscheduler.domain.constraints.ScheduleRules
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.domain.entities.StaffMaps
import java.util.*
/*
Day object used by the PlanGenerator. Compared to ScheduleDay, it additionally contains StaffMaps of staff available
for assigning to each shift type, a link to the preceding day, and it can instantiate a day that should follow it in the
schedule. Map of assigned shifts is gradually modified by multiple steps of the Generator algorithm.
 */
class GeneratorDay(
	val date: Int,
	val previousDay: GeneratorDay?,
	val availableStaff: EnumMap<ShiftType, StaffMap>
) {

	val index = date - 1
	val workShifts: EnumMap<ShiftType, StaffMap> = EnumMap(ShiftType::class.java)
	val allWorkingStaff: StaffMap by lazy(LazyThreadSafetyMode.NONE) {
		workShifts.values.reduce(StaffMap::plus)
	}


	fun followingDay(constraints: StaffConstraints): GeneratorDay {

		fun GeneratorDay.availableStaffMinusVacations(staffOnVacation: StaffMap): GeneratorDay = apply {
			for (type in ShiftType.workShiftTypes)
				this.availableStaff.merge(type, staffOnVacation, StaffMap::minus)
		}

		fun GeneratorDay.availableStaffMinusRules(): GeneratorDay = apply {
			ScheduleRules.values().forEach { it.subtractFromAvailableStaff(this) }
		}

		val nextDate = date + 1
		return GeneratorDay(nextDate, this, allStaffAvailable())
			.availableStaffMinusVacations(constraints.getVacationsOnDate(nextDate))
			.availableStaffMinusRules()
	}

	fun toScheduleDay(): ScheduleDay {

		val completeShifts = EnumMap(workShifts).also { shifts ->
			shifts[ShiftType.DAY_OFF] = StaffMaps.fullStaff() - allWorkingStaff
		}
		return ScheduleDay(date, completeShifts)
	}


	companion object {
		val initial = GeneratorDay(0, null, allStaffAvailable()).apply {
			ShiftType.workShiftTypes.forEach { workShifts[it] = StaffMaps.EMPTY }
		}

		private fun allStaffAvailable(): EnumMap<ShiftType, StaffMap> =
			ShiftType.workShiftTypes.associateWithTo(EnumMap<ShiftType, StaffMap>(ShiftType::class.java)) { StaffMaps.fullStaff() }

	}

}
