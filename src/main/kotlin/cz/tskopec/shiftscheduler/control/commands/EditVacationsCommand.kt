package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel

class EditVacationsCommand(
	private val model: SchedulerModel,
	private val value: Boolean
): EditScheduleCommand<StaffMap>(
	Scheduler.vacations,
	model.vacationsModel.selectedColumnIndices()) {

	override fun execute() {

		val newVacs = model.vacationsModel.selectedColumns().map { col ->
			val selectedStaff = StaffMap(col.selectedRows())
			val alreadyOnVacation = Scheduler.vacations[col.index]
			val newMap = if(value) alreadyOnVacation + selectedStaff else alreadyOnVacation - selectedStaff
			IndexedValue(col.index, newMap)
		}
		Scheduler.updateVacations(newVacs)
	}

	override fun undo() {
		Scheduler.updateVacations(oldValues)
	}
}