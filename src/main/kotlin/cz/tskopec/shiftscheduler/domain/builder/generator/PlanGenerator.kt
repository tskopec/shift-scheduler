package cz.tskopec.shiftscheduler.domain.builder.generator

import cz.tskopec.scheduler.domain.builder.generator.StaffMapIterator
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.domain.entities.StaffMaps
import kotlinx.coroutines.yield


class PlanGenerator(
	private val originalPlan: List<ScheduleDay>,
	private val constraints: StaffConstraints,
	private val skipMap: Array<Boolean>
) {
	//random plan respecting staff constraints and schedule rules
	suspend fun generateRandomPlan(): GeneratorResult = nextDateStep(GeneratorDay.initial)


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


	private suspend fun nextShiftStep(
		currentDay: GeneratorDay,
		currentShiftType: ShiftType,
		alreadyWorkingStaff: StaffMap
	): GeneratorResult {

		fun shouldSkip(day: GeneratorDay): Boolean = skipMap[day.index]

		fun possibleStaffMaps(): Iterator<StaffMap> {

			return if (shouldSkip(currentDay)) { // don't fill in, use the staff map in the original plan
				val staffMap = originalPlan[currentDay.index][currentShiftType]
				val available = currentDay.availableStaff.getValue(currentShiftType)
				if (staffMap !in available)
					StaffMapIterator.EMPTY
				else StaffMapIterator.ofSingleMap(staffMap)
			} else {
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

