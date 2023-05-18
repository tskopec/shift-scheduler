package cz.tskopec.shiftscheduler.domain.entities

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType.*
import java.util.*


data class ScheduleDay(
	val date: Int,
	val shifts: Map<ShiftType, StaffMap>
) {

	val index: Int = date - 1
	val allWorkingStaff: StaffMap = ShiftType.workShiftTypes
		.map { this[it] }
		.fold(StaffMaps.EMPTY, StaffMap::plus)

	init {
		if(date < 1)
			throw IllegalStateException("Date must be greater than 1: $date")
		if(ShiftType.values().any { it !in shifts })
			throw IllegalStateException("Missing shift: $this")
		if(shifts.values.reduce(StaffMap::plus).bits != shifts.values.sumOf { it.bits })
			throw IllegalStateException("Staff has colliding shifts: $this")
	}


	operator fun get(type: ShiftType): StaffMap = shifts.getOrDefault(type, StaffMaps.EMPTY)

	fun getAssignedShiftType(emp: Employee): ShiftType = ShiftType.values().first { emp in this[it] }

	fun isBlank(): Boolean = allWorkingStaff == StaffMaps.EMPTY


	companion object {

		fun blank(date: Int, staffSize: Int = Scheduler.staffSize) = ScheduleDay(
			date, EnumMap(
				mapOf(
					DAY_OFF to StaffMaps.fullStaff(staffSize),
					DAY to StaffMaps.EMPTY,
					NIGHT to StaffMaps.EMPTY,
					MORNING to StaffMaps.EMPTY
				)
			)
		)

		// return new day on which selectedStaff is removed from their current shifts and added to shift of targetType
		fun ScheduleDay.assignShift(targetType: ShiftType, selectedStaff: StaffMap): ScheduleDay {

			val newShifts = shifts.mapValuesTo(EnumMap(ShiftType::class.java)) { (type, staffMap) ->
				if (type == targetType) staffMap + selectedStaff else staffMap - selectedStaff
			}
			return ScheduleDay(this.date, newShifts)
		}
	}
}

