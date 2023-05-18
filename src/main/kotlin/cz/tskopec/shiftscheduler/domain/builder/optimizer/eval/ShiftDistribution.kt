package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType

// numbers of shifts and vacation days assigned to a single employee;
// shifts are also counted by their type and position within the month
class ShiftDistribution(
	val nShifts: Int,
	val byType: IntArray,
	val byPeriod: IntArray,
	val nVacationDays: Int
) {

	// on a given day, increment count of first shift type by 1 and decrement count of the second by 1
	fun update(dayIndex: Int, increment: ShiftType, decrement: ShiftType): ShiftDistribution {

		var newSum = nShifts
		val newByType = byType.copyOf()
		val newByPeriod = byPeriod.copyOf()

		fun updateOfType(type: ShiftType, delta: Int) {
			if (type != ShiftType.DAY_OFF) {
				newSum += delta
				newByType[type.ordinal - 1] += delta
				newByPeriod[periodIndex(dayIndex)] += delta
			}
		}
		updateOfType(increment, 1)
		updateOfType(decrement, -1)
		return ShiftDistribution(newSum, newByType, newByPeriod, nVacationDays)
	}

	companion object {

		// number of periods into which the month is divided so the distribution of shifts in time can be evaluated.
		// Periods are of equal length, except the last one which may be longer by (scheduleLength mod N_PERIODS)
		const val N_PERIODS = 3
		private val PERIOD_LENGTH = Scheduler.scheduleLength / N_PERIODS
		fun periodIndex(dayIndex: Int) = minOf(dayIndex / PERIOD_LENGTH, N_PERIODS - 1)


		fun of(employee: Employee, plan: List<ScheduleDay>, constraints: StaffConstraints): ShiftDistribution {

			val workDays = plan.filter { ShiftType.workShiftTypes.contains(it.getAssignedShiftType(employee)) }
			return ShiftDistribution(
				workDays.size,
				countShiftsByType(workDays.map { it.getAssignedShiftType(employee) }),
				countShiftsByPeriod(workDays.map { it.index }),
				constraints.getVacationDaysCount(employee)
			)
		}

		private fun countShiftsByType(assignedShifts: List<ShiftType>): IntArray {

			return IntArray(ShiftType.workShiftTypes.size).apply{
				assignedShifts.forEach { this[it.ordinal - 1]++ }
			}
		}

		private fun countShiftsByPeriod(workDates: List<Int>): IntArray {

			return IntArray(N_PERIODS){ 0 }.apply{
				workDates.forEach { this[periodIndex(it)]++ }
			}
		}
	}
}
