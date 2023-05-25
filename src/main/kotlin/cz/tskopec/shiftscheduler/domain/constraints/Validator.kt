package cz.tskopec.shiftscheduler.domain.constraints

import cz.tskopec.shiftscheduler.control.Controller
import cz.tskopec.shiftscheduler.domain.Scheduler

// Validates user schedule modifications and supplies the Controller with errors to display
object Validator {

	class Result(
		val dayIndex: Int,
		val presentErrors: Map<ScheduleErrorType, Boolean>
	)

	fun validateUpdate(
		updatedIndices: List<Int>,
		expectedErrors: Set<ScheduleErrorType>
	) {

		val plan = Scheduler.planCopy()
		val constraints = Scheduler.constraintsCopy()

		fun isValid(dayIndex: Int, testedType: ScheduleErrorType): Boolean = when(testedType){
			ScheduleErrorType.ROW_LIMIT -> ScheduleRules.ROW_LIMIT.isDayValid(dayIndex, plan)
			ScheduleErrorType.ILLEGAL_ORDER -> ScheduleRules.SHIFT_ORDERINGS.isDayValid(dayIndex, plan)
			ScheduleErrorType.VACATION_CONFLICT -> constraints.satisfiesVacationPlan(plan[dayIndex])
			ScheduleErrorType.UNDERSTAFFED -> constraints.satisfiesSizeRequirements(plan[dayIndex])
			ScheduleErrorType.IMPOSSIBLE_CONSTRAINTS -> constraints.areSatisfiable(dayIndex)
		}

		updatedIndices.forEach { dayIndex ->
			Controller.showErrors(Result(dayIndex, expectedErrors.associateWith{ !isValid(dayIndex, it)}))
		}
	}
}

