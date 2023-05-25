package cz.tskopec.shiftscheduler.domain.constraints

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap

// Class containing required staff sizes and vacation plans for each day
class StaffConstraints(
	private val sizeRequirements: List<Map<ShiftType, Int>>,
	private val vacations: List<StaffMap>
) {

	fun getRequiredShiftSizeOnDate(date: Int, type: ShiftType): Int = sizeRequirements[date - 1].getValue(type)

	fun satisfiesSizeRequirements(day: ScheduleDay): Boolean = ShiftType.workShiftTypes.all { type ->
		day[type].employeeCount >= sizeRequirements[day.index].getValue(type)
	}

	fun getVacationsOnDate(date: Int): StaffMap = vacations[date - 1]

	fun getVacationDaysCount(emp: Employee) = vacations.count { emp in it }

	fun satisfiesVacationPlan(dayIndex: Int, staff: StaffMap): Boolean = !staff.conflictsWith(vacations[dayIndex])

	fun satisfiesVacationPlan(day: ScheduleDay): Boolean = ShiftType.workShiftTypes.all{ type ->
		satisfiesVacationPlan(day.index, day[type])
	}

	fun areSatisfiable(dayIndex: Int): Boolean {
		val availableStaffSize = Scheduler.staffSize - vacations[dayIndex].employeeCount
		val requiredStaffSize = sizeRequirements[dayIndex].values.sum()
		return requiredStaffSize <= availableStaffSize
	}

	fun areImpossible(): Boolean = (0 until Scheduler.scheduleLength).any { !areSatisfiable(it) }

}
