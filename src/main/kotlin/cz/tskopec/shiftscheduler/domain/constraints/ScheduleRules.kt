package cz.tskopec.shiftscheduler.domain.constraints

import cz.tskopec.shiftscheduler.Settings
import cz.tskopec.shiftscheduler.domain.builder.generator.GeneratorDay
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap

// Rules restricting the shift planning process
enum class ScheduleRules {

	// ensure that no employee can work more than rowLimit shifts in a row
	ROW_LIMIT {

		private val limit = Settings.rowLimit

		override fun validateStaffMap(dayIndex: Int, type: ShiftType, staff: StaffMap, plan: List<ScheduleDay>): Boolean {

			if(type == ShiftType.DAY_OFF)
				throw IllegalArgumentException("Only work shifts can be validated.")

			val left = IntArray(limit + 1) { if (it == 0) staff.bits else 0 }
			val right = IntArray(limit + 1) { if (it == 0) staff.bits else 0 }

			for (i in 1..limit) {
				left[i] = if (dayIndex - i < 0) 0 else plan[dayIndex - i].allWorkingStaff.commonBits(left[i - 1])
				right[i] = if (dayIndex + i >= plan.size) 0 else plan[dayIndex + i].allWorkingStaff.commonBits(right[i - 1])
			}

			for (i in 0..limit) {
				if ((left[i] and right[limit - i]) != 0)
					return false
			}
			return true
		}

		override fun subtractFromAvailableStaff(day: GeneratorDay) {

			if(day.date > limit){

				var currentDay = day.previousDay ?: throw IllegalStateException("Missing previous day reference")
				var staffToSubtract = currentDay.allWorkingStaff
				repeat(limit - 1){
					currentDay = currentDay.previousDay ?: throw IllegalStateException("Missing previous day reference")
					staffToSubtract = staffToSubtract.commonStaff(currentDay.allWorkingStaff)
				}
				for((type, staff) in day.availableStaff){
					day.availableStaff[type] = staff - staffToSubtract
				}
			}
		}

	},

	// ensure that no employee can work consecutive shifts in certain forbidden orderings
	SHIFT_ORDERINGS {

		private val illegalOrderings = Settings.illegalOrderings

		override fun validateStaffMap(dayIndex: Int, type: ShiftType, staff: StaffMap, plan: List<ScheduleDay>): Boolean {

			if(type == ShiftType.DAY_OFF)
				throw IllegalArgumentException("Only work shifts can be validated.")

			fun checkPrevious(): Boolean
					= illegalOrderings.none{ (first, second) ->
				type == second && plan[dayIndex - 1][first].conflictsWith(staff)
			}
			fun checkNext(): Boolean
					= illegalOrderings.none { (first, second) ->
				type == first && plan[dayIndex + 1][second].conflictsWith(staff)
			}
			return (dayIndex == 0 || checkPrevious()) && (dayIndex == plan.size - 1 || checkNext())
		}

		override fun subtractFromAvailableStaff(day: GeneratorDay) {

			val previousDay = day.previousDay ?: return
			illegalOrderings.forEach { (first, second) ->
				day.availableStaff[second] = day.availableStaff.getValue(second) - previousDay.workShifts.getValue(first)
			}
		}
	};

	// Return true if assigning this staffMap to this shift type on schedule day that is located at dayIndex within the plan
	// does not break this rule
	abstract fun validateStaffMap(
		dayIndex: Int,
		type: ShiftType,
		staff: StaffMap,
		plan: List<ScheduleDay>
	): Boolean


	// Remove from available staff the employees who cannot be assigned to work shift because of this rule.
	// Only takes into account days preceding the given day, since PlanGenerator proceeds from the beginning
	// of the month to its end.
	abstract fun subtractFromAvailableStaff(day: GeneratorDay)


	fun isDayValid(dayIndex: Int, plan: List<ScheduleDay>) = ShiftType.workShiftTypes.all { type ->
		validateStaffMap(dayIndex, type, plan[dayIndex][type], plan)
	}

	companion object {

		fun isStaffMapValid(
			dayIndex: Int,
			type: ShiftType,
			staff: StaffMap,
			plan: List<ScheduleDay>
		): Boolean = ScheduleRules.values().all { it.validateStaffMap(dayIndex, type, staff, plan) }
	}

}
