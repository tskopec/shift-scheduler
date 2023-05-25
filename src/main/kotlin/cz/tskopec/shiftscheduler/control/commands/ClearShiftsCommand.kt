package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel

class ClearShiftsCommand(
	model: SchedulerModel
): EditScheduleCommand<ScheduleDay>(
	editedList = Scheduler.plan,
	selectedIndices = model.dateCellsModel.selectedColumnIndices().ifEmpty { Scheduler.allPlanIndices }){


	override fun execute() {

		val blankDays = editedIndices.map { ScheduleDay.blank(it + 1) }
		Scheduler.updatePlan(blankDays)
	}

	override fun undo() {
		Scheduler.updatePlan(oldValues.map { it.value })
	}
}