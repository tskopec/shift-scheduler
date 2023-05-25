package cz.tskopec.shiftscheduler.domain.builder.generator

import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.domain.entities.StaffMaps
import kotlinx.coroutines.yield

/*
Tries to generate random plan respecting the rules and constraints by a recursive backtracking algorithm. If no such plan
exists, Result.DeadEnd is returned
 */
class PlanGenerator(
	private val originalPlan: List<ScheduleDay>,
	private val constraints: StaffConstraints,
	private val skipMap: Array<Boolean>
) {

	suspend fun generateRandomPlan(): GeneratorResult = nextDateStep(GeneratorDay.initial)

	// add next day to the plan
	private suspend fun nextDateStep(
		lastDay: GeneratorDay
	): GeneratorResult {

		with(lastDay) {
			return if (date == originalPlan.size){
				GeneratorResult.Success(lastDay)
			}
			else nextShiftStep(followingDay(constraints), ShiftType.DAY, StaffMaps.EMPTY)
		}
	}
	/*
	Assign some staff members to the current shift on the current day and proceed to the next step. If next step returns
	Result.DeadEnd, try assigning different staff combination. When all staff combinations have been tried without success,
	go back to the previous step by also returning Result.DeadEnd.
	 */
	private suspend fun nextShiftStep(
		currentDay: GeneratorDay,
		currentShiftType: ShiftType,
		alreadyWorkingStaff: StaffMap
	): GeneratorResult {

		fun shouldSkip(day: GeneratorDay): Boolean = skipMap[day.index]

		fun possibleStaffMaps(): Iterator<StaffMap> {

			// If user has not specified this plan index to be modified, skip it and use the staff map in the original plan
			return if (shouldSkip(currentDay)) {
				val staffMap = originalPlan[currentDay.index][currentShiftType]
				val available = currentDay.availableStaff.getValue(currentShiftType)
				if (staffMap !in available)
					StaffMapIterator.EMPTY
				else StaffMapIterator.ofSingleMap(staffMap)
			} else {
				// All possible staff member combinations that can be assigned to this shift according to the rules and constraints
				return StaffMapIterator(
					availableStaffMap = currentDay.availableStaff.getValue(currentShiftType) - alreadyWorkingStaff,
					requiredStaffSize = constraints.getRequiredShiftSizeOnDate(currentDay.date, currentShiftType)
				)
			}
		}

		for (staffMap in possibleStaffMaps()) {

			yield()
			currentDay.workShifts[currentShiftType] = staffMap
			val nextStepResult = if (currentShiftType.isLastWorkType())
				nextDateStep(currentDay)
			else
				nextShiftStep(currentDay, currentShiftType.next(), alreadyWorkingStaff + staffMap)
			return when (nextStepResult) {
				is GeneratorResult.Success -> nextStepResult
				is GeneratorResult.DeadEnd -> continue
			}
		}
		return GeneratorResult.DeadEnd
	}
}

